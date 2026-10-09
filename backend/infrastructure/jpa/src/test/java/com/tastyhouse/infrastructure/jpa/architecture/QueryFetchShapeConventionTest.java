package com.tastyhouse.infrastructure.jpa.architecture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QueryFetchShapeConventionTest {

    private static final Path QUERY_SOURCE_ROOT =
        Path.of("src/main/java/com/tastyhouse/infrastructure/jpa");

    private static final Pattern INLINE_OPTIONAL =
        Pattern.compile("Optional\\.ofNullable\\(\\s*queryFactory");

    private static final Pattern TOP_LEVEL_SELECT_ONE =
        Pattern.compile("queryFactory\\s*\\.selectOne\\(\\)");

    private static final Pattern LOCAL_EXISTS_VARIABLE =
        Pattern.compile("\\w+\\s+\\w+\\s*=\\s*queryFactory\\s*\\.selectOne\\(\\)");

    private static final Pattern COUNT_EXISTS =
        Pattern.compile("count\\s*!=\\s*null\\s*&&\\s*count\\s*>\\s*0");

    private static final Pattern SINGLE_SOURCE_SELECT_FROM =
        Pattern.compile("\\.select\\((\\w+JpaEntity)\\)\\s*\\.from\\(\\1\\)");

    private static final Pattern ENTITY_LOAD_VARIABLE =
        Pattern.compile("\\w+JpaEntity\\s+(\\w+)\\s*=\\s*queryFactory\\s*\\.selectFrom\\(");

    private static final Pattern SELECT_FROM =
        Pattern.compile("\\.selectFrom\\(");

    private static final String EXISTS_TERMINATOR = ".fetchFirst() != null";

    private static final int MIN_TOP_LEVEL_SELECT_ONE = 45;

    private static final int MIN_SELECT_FROM = 100;

    @Test
    @DisplayName("단건 결과는 지역 변수로 받은 뒤 Optional.ofNullable로 감싼다")
    void singleResultShouldBeAssignedBeforeWrapping() {
        assertThat(violations(INLINE_OPTIONAL)).isEmpty();
    }

    @Test
    @DisplayName("존재 확인은 selectOne 결과를 지역 변수로 받지 않는다")
    void existsShouldNotAssignSelectOneToLocalVariable() {
        assertThat(violations(LOCAL_EXISTS_VARIABLE)).isEmpty();
    }

    @Test
    @DisplayName("최상위 selectOne 문장은 fetchFirst() != null로 끝난다")
    void topLevelSelectOneShouldEndWithFetchFirstNotNull() {
        List<String> violations = new ArrayList<>();
        int checked = 0;

        for (Path source : javaSources()) {
            String text = read(source);
            Matcher matcher = TOP_LEVEL_SELECT_ONE.matcher(text);
            while (matcher.find()) {
                checked++;
                int end = text.indexOf(';', matcher.end());
                String statement = text.substring(matcher.start(), end).replaceAll("\\s+", " ").strip();
                if (!statement.endsWith(EXISTS_TERMINATOR)) {
                    violations.add(location(source, text, matcher.start()));
                }
            }
        }

        assertThat(checked).isGreaterThanOrEqualTo(MIN_TOP_LEVEL_SELECT_ONE);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("존재 확인에 count() > 0을 쓰지 않는다")
    void existsShouldNotUseCount() {
        assertThat(violations(COUNT_EXISTS)).isEmpty();
    }

    @Test
    @DisplayName("단일 소스 엔티티 로드는 select(x).from(x) 대신 selectFrom(x)를 쓴다")
    void singleSourceEntityLoadShouldUseSelectFrom() {
        assertThat(violations(SINGLE_SOURCE_SELECT_FROM)).isEmpty();
    }

    @Test
    @DisplayName("selectFrom으로 로드한 JpaEntity 지역 변수 이름은 entity다")
    void entityLoadVariableShouldBeNamedEntity() {
        List<String> violations = new ArrayList<>();
        int selectFromCount = 0;

        for (Path source : javaSources()) {
            String text = read(source);
            Matcher counter = SELECT_FROM.matcher(text);
            while (counter.find()) {
                selectFromCount++;
            }
            Matcher matcher = ENTITY_LOAD_VARIABLE.matcher(text);
            while (matcher.find()) {
                if (!matcher.group(1).equals("entity")) {
                    violations.add(location(source, text, matcher.start()) + " (" + matcher.group(1) + ")");
                }
            }
        }

        assertThat(selectFromCount).isGreaterThanOrEqualTo(MIN_SELECT_FROM);
        assertThat(violations).isEmpty();
    }

    private List<String> violations(Pattern pattern) {
        List<String> violations = new ArrayList<>();
        for (Path source : javaSources()) {
            String text = read(source);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                violations.add(location(source, text, matcher.start()));
            }
        }
        return violations;
    }

    private String location(Path source, String text, int offset) {
        int line = 1;
        for (int i = 0; i < offset; i++) {
            if (text.charAt(i) == '\n') {
                line++;
            }
        }
        return source + ":" + line;
    }

    private List<Path> javaSources() {
        try (Stream<Path> paths = Files.walk(QUERY_SOURCE_ROOT)) {
            List<Path> sources = paths.filter(path -> path.toString().endsWith(".java")).toList();
            assertThat(sources).isNotEmpty();
            return sources;
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
}
