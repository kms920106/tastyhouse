package com.tastyhouse.application.architecture;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaType;

import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.application.shared.marker.BatchApp;
import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.application.shared.marker.WebApp;

/**
 * Command record의 <b>앱 소속을 유도</b>한다 — 챕터 03에서 패키지가 평탄화되며 소속을 알려주던
 * 마지막 단서(패키지 접두어)가 사라진 자리를 메운다.
 *
 * <p>Command record에는 마커를 달지 않는다. 300여 개 record에 마커를 손으로 다는 것은 누락이
 * 확실하고, 무엇보다 <b>소속이 이미 결정돼 있다</b> — 그 record를 시그니처에 쓰는 UseCase의
 * 마커가 곧 소속이기 때문이다. 손으로 다는 두 번째 진실원을 만들지 않고 유도한다.
 *
 * <p><b>유도 규칙</b>: {@code apps(R)} = R을 파라미터·반환 타입·제네릭 인자로 쓰는 마커 UseCase의
 * 마커 집합 ∪ R을 컴포넌트로 품는 record의 {@code apps}. 뒤쪽 항이 <b>전이 폐쇄</b>로,
 * {@code OrderLineCommand}처럼 부모 Command의 컴포넌트로만 등장하는 중첩 Command 10개를 위한
 * 것이다. 고정점에 도달할 때까지 반복한다.
 *
 * <p>결과 해석 — {@code apps(R)}의 크기가 <b>0이면 고아</b>(어느 UseCase도 쓰지 않는 죽은 코드),
 * <b>2 이상이면 앱 간 공유</b>(경계 위반)로 둘 다 규칙 위반이다. 챕터 02의 개명으로 앱 간
 * 동명 Command가 없어 유도 결과가 유일하다.
 *
 * <p><b>testFixtures에 두는 이유</b>: api 4모듈의 {@code adaptersShouldOnlyUseOwnAppUseCases}가
 * 같은 유도를 필요로 한다(컨트롤러가 의존하는 Command record가 자기 앱 것인지 판정). 같은 파일을
 * 복제하면 두 벌이 갈라지므로 {@code java-test-fixtures}로 공유한다 —
 * api build.gradle의 {@code testImplementation(testFixtures(project(':application')))}.
 */
public final class AppOwnership {

    /**
     * <b>유도로 잡히지 않는 Command record — 소속을 명시한다.</b>
     *
     * <p>{@code ShopStorePriceVerificationItemCommand}는 multipart의 <b>문자열 파트</b>로 들어온다.
     * 컨트롤러도 Request record도 domain-free여야 해 파싱을 할 수 없으므로, Command가 원문을
     * {@code String items}로 담아 넘기고 {@code ShopStorePriceVerificationCommandService}가
     * {@code ObjectMapper}로 이 record 목록으로 역직렬화한다(backend/CLAUDE.md의
     * "multipart 문자열 파트의 파싱 위치" 참조).
     *
     * <p>그래서 이 record는 어느 UseCase 시그니처에도, 어느 부모 Command의 컴포넌트로도 등장하지
     * 않는다 — 유도가 닿을 수 없는 <b>정상</b> 형태이지 죽은 코드가 아니다. 이 목록에 담아
     * 소속을 못 박는다.
     *
     * <p><b>이 목록에 새 항목을 추가하지 않는다.</b> 고아로 잡히는 record는 대개 진짜 죽은 코드이므로,
     * 추가하기 전에 그 record를 <b>어디서 만드는지</b>를 먼저 찾는다. 여기 담을 수 있는 것은
     * "런타임 역직렬화로만 생성되어 정적 참조가 존재할 수 없는" 경우뿐이다.
     */
    private static final Map<String, Class<? extends Annotation>> DESERIALIZED_COMMANDS = Map.of(
        "com.tastyhouse.application.shop.port.in.ShopStorePriceVerificationItemCommand", CeoApp.class);

    /** 앱 마커 4종. 이 목록이 유도의 술어이자 결과 집합의 원소다. */
    public static final List<Class<? extends Annotation>> MARKERS =
        List.of(WebApp.class, AdminApp.class, CeoApp.class, BatchApp.class);

    private AppOwnership() {
    }

    /**
     * {@code ..port.in..}의 record마다 소속 앱 집합을 유도한다.
     *
     * @param classes 대상 클래스 집합. {@code com.tastyhouse.application} 전체를 넣는다
     * @return record → 소속 앱 마커 집합. 대상 record는 전부 키로 등장하며, 고아는 빈 집합을 갖는다
     */
    public static Map<JavaClass, Set<Class<? extends Annotation>>> derive(JavaClasses classes) {
        List<JavaClass> commandRecords = classes.stream()
            .filter(AppOwnership::isCommandRecord)
            .toList();

        Map<JavaClass, Set<Class<? extends Annotation>>> apps = new HashMap<>();
        for (JavaClass record : commandRecords) {
            Set<Class<? extends Annotation>> seed = new LinkedHashSet<>();
            Class<? extends Annotation> declared = DESERIALIZED_COMMANDS.get(record.getName());
            if (declared != null) {
                seed.add(declared);
            }
            apps.put(record, seed);
        }

        // 1단계 — 마커 UseCase의 시그니처에 직접 등장하는 record.
        classes.stream()
            .filter(JavaClass::isInterface)
            .filter(c -> c.getPackageName().contains(".port.in"))
            .forEach(useCase -> {
                Set<Class<? extends Annotation>> useCaseMarkers = markersOf(useCase);
                if (useCaseMarkers.isEmpty()) {
                    return;
                }
                for (JavaClass referenced : signatureTypes(useCase)) {
                    Set<Class<? extends Annotation>> target = apps.get(referenced);
                    if (target != null) {
                        target.addAll(useCaseMarkers);
                    }
                }
            });

        // 2단계 — 중첩 Command로의 전이 폐쇄. 부모의 소속이 컴포넌트로 흘러내린다.
        boolean changed = true;
        while (changed) {
            changed = false;
            for (JavaClass parent : commandRecords) {
                Set<Class<? extends Annotation>> parentApps = apps.get(parent);
                if (parentApps.isEmpty()) {
                    continue;
                }
                for (JavaClass component : componentTypes(parent)) {
                    Set<Class<? extends Annotation>> childApps = apps.get(component);
                    if (childApps != null && childApps.addAll(parentApps)) {
                        changed = true;
                    }
                }
            }
        }

        return apps;
    }

    /** 이 클래스에 붙은 앱 마커 집합(0~4개). */
    public static Set<Class<? extends Annotation>> markersOf(JavaClass javaClass) {
        Set<Class<? extends Annotation>> found = new LinkedHashSet<>();
        for (Class<? extends Annotation> marker : MARKERS) {
            if (javaClass.isAnnotatedWith(marker)) {
                found.add(marker);
            }
        }
        return found;
    }

    /** {@code ..port.in..}에 사는 record인가(= Command record). */
    public static boolean isCommandRecord(JavaClass javaClass) {
        return javaClass.isRecord() && javaClass.getPackageName().contains(".port.in");
    }

    /**
     * 인터페이스의 모든 메서드 시그니처에 등장하는 타입(파라미터·반환 + 제네릭 인자).
     *
     * <p>{@code List<XxxCommand>}처럼 컬렉션에 담겨 넘어오는 형태를 놓치지 않으려면 제네릭 인자까지
     * 펼쳐야 한다 — raw 타입만 보면 {@code List}만 잡힌다.
     */
    private static Set<JavaClass> signatureTypes(JavaClass useCase) {
        Set<JavaClass> types = new LinkedHashSet<>();
        for (JavaMethod method : useCase.getMethods()) {
            collect(method.getReturnType(), types);
            method.getParameterTypes().forEach(type -> collect(type, types));
        }
        return types;
    }

    /** record 컴포넌트의 타입(제네릭 인자 포함). */
    private static Set<JavaClass> componentTypes(JavaClass record) {
        Set<JavaClass> types = new LinkedHashSet<>();
        record.getFields().forEach(field -> {
            collect(field.getType(), types);
            collect(field.getRawType(), types);
        });
        return types;
    }

    private static void collect(JavaType type, Set<JavaClass> into) {
        collect(type, into, new HashSet<>());
    }

    private static void collect(JavaType type, Set<JavaClass> into, Set<JavaType> visited) {
        if (type == null || !visited.add(type)) {
            return;
        }
        into.add(type.toErasure());
        if (type instanceof com.tngtech.archunit.core.domain.JavaParameterizedType parameterized) {
            for (JavaType argument : parameterized.getActualTypeArguments()) {
                collect(argument, into, visited);
            }
        }
    }

    /** 사람이 읽는 진단 문자열(위반 메시지용). */
    public static String describe(Set<Class<? extends Annotation>> markers) {
        if (markers.isEmpty()) {
            return "(없음)";
        }
        List<String> names = new ArrayList<>();
        markers.forEach(marker -> names.add(marker.getSimpleName()));
        return String.join(", ", names);
    }
}
