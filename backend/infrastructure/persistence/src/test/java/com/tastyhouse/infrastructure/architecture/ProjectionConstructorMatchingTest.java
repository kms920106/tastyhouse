package com.tastyhouse.infrastructure.architecture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.ClassUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code Projections.constructor(Xxx.class, ...)} 호출의 <b>인자 개수</b>가 대상 record의
 * canonical 생성자 파라미터 개수와 일치하는지 검증하는 가드 테스트.
 *
 * <p><b>왜 필요한가</b>: 읽기 경로 포트화(챕터 04)로 Result record가 infrastructure-module 밖
 * 읽기 계약 패키지 {@code com.tastyhouse.application..port.out}로 이동하면서 {@code @QueryProjection}을 쓸 수 없게 되었다.
 * {@code @QueryProjection}은 생성된 {@code QXxxResult} 타입이 <b>컴파일 타임에</b> 생성자 시그니처를
 * 강제해 주었지만, 그 대체인 {@code Projections.constructor}는 {@code Class<?>}와 가변인자
 * {@code Expression<?>...}를 받으므로 <b>개수·타입이 어긋나도 컴파일이 통과</b>한다. 실패는 해당
 * 쿼리가 실제로 실행되는 순간 아래 예외로 500이 되어서야 드러난다.
 *
 * <pre>
 * com.querydsl.core.types.ExpressionException: No constructor found for class ... with parameters: [...]
 * </pre>
 *
 * <p>즉 이 전환은 <b>컴파일 게이트 하나를 잃는</b> 변경이며, 그 자리를 이 테스트가 메운다.
 * 개수 불일치는 정적으로 확실히 판별할 수 있으므로 여기서 잡는다. 타입 불일치까지 정적으로 판별하려면
 * QueryDSL 표현식의 제네릭 타입을 추론해야 해 소스 수준 파싱으로는 신뢰할 수 없고, 그 층은
 * 짝 가드인 {@code QueryResultRecordVisibilityTest}(가시성)와 컨텍스트별 기존 조회 테스트가 맡는다.
 *
 * <p>스캔 대상은 소스 파일이다 — 바이트코드에는 가변인자가 배열로 뭉쳐 있어 호출 지점의 인자 개수를
 * 복원할 수 없다.
 */
class ProjectionConstructorMatchingTest {

    private static final Path QUERY_SOURCE_ROOT =
        Path.of("src/main/java/com/tastyhouse/infrastructure");

    /** {@code Projections.constructor(Xxx.class,} 로 시작하는 호출 지점. */
    private static final Pattern CALL_START =
        Pattern.compile("Projections\\.constructor\\(\\s*([A-Za-z0-9_.]+)\\.class\\s*,");

    @Test
    @DisplayName("Projections.constructor 인자 개수가 대상 record 생성자 파라미터 개수와 일치해야 한다")
    void projectionArgumentCountShouldMatchConstructor() {
        List<String> mismatches = new ArrayList<>();
        int checked = 0;

        for (Path source : javaSources()) {
            String text = read(source);
            List<String> imports = importsOf(text);
            Matcher matcher = CALL_START.matcher(text);

            while (matcher.find()) {
                String simpleName = matcher.group(1);
                Class<?> target = resolve(simpleName, imports);
                if (target == null || !target.isRecord()) {
                    // 프로젝트 외부 타입이거나 record가 아니면(Row 조립용 일반 클래스 등) 대상이 아니다.
                    continue;
                }

                List<String> arguments = readArguments(text, matcher.end());
                if (arguments == null) {
                    continue;
                }
                int actual = arguments.size();

                List<Integer> accepted = publicConstructorArities(target);
                checked++;
                if (!accepted.contains(actual)) {
                    mismatches.add("%s: Projections.constructor(%s.class, ...) 인자 %d개 ↔ public 생성자 파라미터 %s"
                        .formatted(source.getFileName(), simpleName, actual, accepted));
                    continue;
                }

                String reordering = detectReordering(target, arguments);
                if (reordering != null) {
                    mismatches.add("%s: Projections.constructor(%s.class, ...) %s"
                        .formatted(source.getFileName(), simpleName, reordering));
                }
            }
        }

        // 스캔이 아무것도 못 찾으면 규칙이 공허하게 통과하므로, 대상이 존재하는 것 자체를 먼저 검증한다.
        assertThat(checked)
            .as("Projections.constructor 호출을 하나도 찾지 못했다 — 스캔 경로(%s)나 패턴이 잘못되었을 수 있다",
                QUERY_SOURCE_ROOT)
            .isPositive();

        assertThat(mismatches)
            .as("""
                아래 select 절의 인자 개수가 대상 record의 생성자와 다르다. Projections.constructor는
                Class<?>와 가변인자를 받으므로 컴파일이 통과하고, 해당 조회가 실행될 때 QueryDSL
                ExpressionException(No constructor found)으로 500이 난다 — 이 테스트 외에는 걸러낼 방법이 없다.
                record 컴포넌트 선언 순서와 select 절 인자 순서를 하나씩 대조하라.""")
            .isEmpty();
    }

    /**
     * 여는 괄호부터 짝이 맞는 닫는 괄호까지를 훑어, 최상위 depth의 콤마로 인자를 잘라 낸다.
     * 중첩 호출({@code shopJpaEntity.id.coalesce(0L)})·문자열·문자 리터럴은 인자 구분자로 세지 않는다.
     */
    private List<String> readArguments(String text, int afterFirstComma) {
        List<String> arguments = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 1;
        boolean inString = false;
        boolean inChar = false;

        for (int i = afterFirstComma; i < text.length(); i++) {
            char c = text.charAt(i);
            char prev = text.charAt(i - 1);

            if (inString) {
                if (c == '"' && prev != '\\') {
                    inString = false;
                }
                continue;
            }
            if (inChar) {
                if (c == '\'' && prev != '\\') {
                    inChar = false;
                }
                continue;
            }

            switch (c) {
                case '"' -> inString = true;
                case '\'' -> inChar = true;
                case '(' -> depth++;
                case ')' -> {
                    depth--;
                    if (depth == 0) {
                        arguments.add(current.toString());
                        return arguments;
                    }
                }
                case ',' -> {
                    if (depth == 1) {
                        arguments.add(current.toString());
                        current.setLength(0);
                        continue;
                    }
                }
                default -> { }
            }
            current.append(c);
        }
        return null;
    }

    /**
     * <b>같은 개수의 인자 순서 뒤바뀜</b>을 이름으로 잡는다 — 개수 검사만으로는 통과하지만
     * 조용히 틀린 값을 돌려주는, 이 리포의 반복 사고 유형이다.
     *
     * <p>판별 방법: QueryDSL 경로의 <em>마지막 프로퍼티 이름</em>(예 {@code memberJpaEntity.fullName} →
     * {@code fullName}, {@code memberJpaEntity.phoneNumber.value} → {@code value})을 같은 자리의 record
     * 컴포넌트 이름과 맞춰 본다. 컬럼명과 컴포넌트명이 다른 것은 정상이므로(별칭·VO 언랩·표현용 개명)
     * <b>불일치 자체를 실패로 삼지 않는다</b>. 실패로 보는 것은 <b>순열</b>인 경우다 — 즉 이 호출의
     * 이름 집합과 record 컴포넌트 이름 집합이 같은데 순서만 다를 때다. 그때는 "이름은 다 있는데 자리가
     * 어긋났다"는 뜻이라 뒤바뀜이 거의 확실하다.
     *
     * <p>이 방식은 오탐이 없는 대신 놓치는 경우가 있다(이름이 애초에 다른 투영). 남는 층은 컨텍스트별
     * 조회 테스트와 코드 리뷰가 맡는다.
     */
    private String detectReordering(Class<?> target, List<String> arguments) {
        List<String> components = java.util.Arrays.stream(target.getRecordComponents())
            .map(java.lang.reflect.RecordComponent::getName)
            .toList();
        if (components.size() != arguments.size()) {
            return null;
        }

        List<String> argumentNames = arguments.stream().map(this::trailingPropertyName).toList();

        // 이름을 뽑을 수 있는 자리만 본다. stringValue()·coalesce() 같은 호출이 섞인 자리는 null이 되는데,
        // 그 자리 하나 때문에 검사 전체를 포기하면 실제 투영 대부분이 빠져나간다.
        //
        // 판정: "같은 이름이 서로 다른 자리에 놓인" 쌍이 있으면 뒤바뀜으로 본다. 즉 인자 이름 x가
        // i번 자리에 있는데 record에서 x는 j번(≠ i) 컴포넌트인 경우다. 컬럼명과 컴포넌트명이 아예
        // 다른 자리(별칭·VO 언랩 — phoneNumber.value → phoneNumber)는 애초에 후보가 아니므로
        // 오탐을 만들지 않는다.
        List<String> swapped = new ArrayList<>();
        for (int i = 0; i < argumentNames.size(); i++) {
            String name = argumentNames.get(i);
            if (name == null || name.equals(components.get(i))) {
                continue;
            }
            // 이름이 양쪽에서 정확히 한 번씩만 나올 때만 자리를 논할 수 있다.
            // 서로 다른 엔티티의 같은 컬럼(shopJpaEntity.name·productJpaEntity.name)이 각각
            // shopName·name으로 투영되는 형태가 흔해, 중복 이름으로 자리를 추론하면 오탐이 된다.
            if (java.util.Collections.frequency(argumentNames, name) != 1
                || java.util.Collections.frequency(components, name) != 1) {
                continue;
            }
            int expectedIndex = components.indexOf(name);
            if (expectedIndex >= 0 && argumentNames.get(expectedIndex) != null) {
                swapped.add("%s: select %d번 자리 ↔ record %d번 컴포넌트".formatted(name, i, expectedIndex));
            }
        }
        if (swapped.isEmpty()) {
            return null;
        }

        return "인자 순서가 record 컴포넌트 순서와 어긋난다 — " + String.join(", ", swapped)
            + " (select=%s ↔ record=%s)".formatted(argumentNames, components);
    }

    /** {@code a.b.c} 형태의 단순 경로에서 마지막 이름을 뽑는다. 호출·연산이 섞이면 {@code null}. */
    private String trailingPropertyName(String argument) {
        String trimmed = argument.strip();
        if (!trimmed.matches("[A-Za-z_$][\\w$]*(\\.[A-Za-z_$][\\w$]*)+")) {
            return null;
        }
        return trimmed.substring(trimmed.lastIndexOf('.') + 1);
    }

    /**
     * {@code Projections.constructor}가 실제로 탐색하는 후보 — {@code Class#getConstructors()},
     * 즉 <b>public 생성자</b>의 파라미터 개수 목록이다.
     *
     * <p>canonical 생성자만 보면 안 된다: 1:N 컬렉션처럼 한 번의 투영으로 채울 수 없는 필드를 뺀
     * <em>좁은 시그니처</em>의 투영 전용 생성자를 따로 두고 DAO가 그쪽을 부르는 형태가 이 리포에 있다
     * (예: {@code OrderDetailResult} — 상품 라인·결제 제외). 그 경우 canonical과 개수가 다른 것이 정상이다.
     *
     * <p>public 생성자가 하나도 없으면(= record가 public이 아니면) 빈 목록이 되어 여기서도 실패로
     * 드러나지만, 그 진단은 짝 가드인 {@code QueryResultRecordVisibilityTest}가 더 정확히 설명한다.
     */
    private List<Integer> publicConstructorArities(Class<?> target) {
        List<Integer> arities = new ArrayList<>();
        for (Constructor<?> constructor : target.getConstructors()) {
            arities.add(constructor.getParameterCount());
        }
        return arities;
    }

    private Class<?> resolve(String simpleName, List<String> imports) {
        if (simpleName.contains(".")) {
            return load(simpleName);
        }
        for (String imported : imports) {
            if (imported.endsWith("." + simpleName)) {
                return load(imported);
            }
        }
        return null;
    }

    private Class<?> load(String fullyQualifiedName) {
        try {
            return ClassUtils.resolveClassName(fullyQualifiedName, getClass().getClassLoader());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private List<String> importsOf(String text) {
        return text.lines()
            .filter(line -> line.startsWith("import ") && !line.startsWith("import static "))
            .map(line -> line.substring("import ".length()).replace(";", "").trim())
            .toList();
    }

    private List<Path> javaSources() {
        try (Stream<Path> paths = Files.walk(QUERY_SOURCE_ROOT)) {
            return paths.filter(path -> path.toString().endsWith(".java")).toList();
        } catch (IOException e) {
            throw new UncheckedIOException("infrastructure 소스 스캔에 실패했다: " + QUERY_SOURCE_ROOT, e);
        }
    }

    private String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new UncheckedIOException("소스 파일을 읽지 못했다: " + path, e);
        }
    }

    /** 사용하지 않는 경고 방지용 — 가시성 검사는 짝 가드가 담당한다. */
    @SuppressWarnings("unused")
    private static boolean isPublic(Class<?> type) {
        return Modifier.isPublic(type.getModifiers());
    }
}
