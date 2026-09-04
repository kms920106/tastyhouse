package com.tastyhouse.application.architecture;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.application.shared.marker.BatchApp;
import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.application.shared.marker.WebApp;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * <b>앱 격리 — 챕터 03에서 마커 애노테이션 기준으로 전면 재작성.</b>
 *
 * <p>챕터 01이 4개 모듈을 하나로 합치며 잃은 컴파일 게이트를 슬라이스 규칙으로 대체했고,
 * 그 슬라이스의 근거는 <b>패키지 접두어</b>({@code com.tastyhouse.(*)application..})였다.
 * 챕터 03이 패키지를 {@code com.tastyhouse.application} 하나로 평탄화하면서 그 근거가 사라졌으므로,
 * 이 파일의 모든 규칙을 {@link WebApp}·{@link AdminApp}·{@link CeoApp}·{@link BatchApp}
 * <b>마커 술어</b>로 다시 썼다.
 *
 * <p>마커는 ArchUnit의 술어이기만 한 것이 아니라 <b>스프링 스캔의 유일한 포함 기준</b>이기도 하다
 * ({@code useDefaultFilters = false}). 그래서 아래 {@code beansShouldHaveExactlyOneAppMarker}는
 * 단순한 규약 검사가 아니라 <b>마커 누락 = 빈 미등록</b>을 빌드 시점에 잡는 유일한 방어선이다 —
 * 마커를 빠뜨린 {@code @Service}는 컴파일이 통과하고, 실패는 그 빈이 처음 필요해지는
 * 기동 시점에야 {@code NoSuchBeanDefinitionException}으로 드러난다.
 */
class AppIsolationTest {

    private static final List<Class<? extends Annotation>> MARKERS = AppOwnership.MARKERS;

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.application");

    /**
     * 앱 간 수평 의존 금지 — 마커 4종의 4×3 조합 12개를 전부 검사한다.
     *
     * <p>앱이 공유해도 되는 것은 domain-module과 읽기 계약({@code ..port.out..})뿐이다. 그 둘은
     * 마커를 달지 않으므로 이 규칙의 대상이 아니며, 공유되는 것이 맞다.
     *
     * <p>슬라이스 규칙이 아니라 12개 규칙을 명시적으로 도는 이유는 <b>공허 통과가 없기 때문</b>이다.
     * 슬라이스는 슬라이스가 0개여도 조용히 통과해 별도 anchor가 필요했으나, 마커 술어는 대상이
     * 0건이면 {@code noClasses()}가 그대로 통과하므로 아래 {@code markerBeanCounts}가 그 자리를 잇는다.
     */
    @Test
    void appsShouldNotDependOnEachOther() {
        for (Class<? extends Annotation> from : MARKERS) {
            for (Class<? extends Annotation> to : MARKERS) {
                if (from == to) {
                    continue;
                }
                noClasses()
                    .that().areAnnotatedWith(from)
                    .should().dependOnClassesThat().areAnnotatedWith(to)
                    .because(from.getSimpleName() + "는 " + to.getSimpleName() + "에 의존하지 않는다"
                        + " — 앱이 공유하는 것은 domain-module과 읽기 계약뿐이다")
                    .check(classes);
            }
        }
    }

    /**
     * 빈은 마커를 <b>정확히 1개</b> 단다.
     *
     * <p>0개면 어느 앱에도 뜨지 않고(스캔 기준을 만족하지 못한다), 2개 이상이면 두 앱에 함께 떠
     * 앱 격리가 무너진다. {@code *ApplicationConfig} 4개는 스캔의 <b>주체</b>지 대상이 아니므로
     * {@code @Configuration}으로 제외한다.
     */
    @Test
    void beansShouldHaveExactlyOneAppMarker() {
        classes()
            .that().areAnnotatedWith(Service.class).or().areAnnotatedWith(Component.class)
            .and().areNotAnnotatedWith(Configuration.class)
            .should(haveExactlyOneAppMarker())
            .because("마커 없는 @Service는 어느 앱에도 뜨지 않는다(useDefaultFilters = false)")
            .check(classes);
    }

    /**
     * UseCase 인터페이스도 마커를 정확히 1개 단다.
     *
     * <p>UseCase는 빈이 아니지만 마커가 필요하다 — 아래
     * {@code commandRecordsShouldBelongToExactlyOneApp}의 유도가 <b>UseCase의 마커에서 출발</b>하므로,
     * 여기가 비면 그 아래 Command record 300여 개가 통째로 고아로 판정된다.
     */
    @Test
    void useCasesShouldHaveExactlyOneAppMarker() {
        classes()
            .that().resideInAPackage("..port.in..").and().areInterfaces()
            .should(haveExactlyOneAppMarker())
            .because("Command record의 앱 소속은 이 마커에서 유도된다")
            .check(classes);
    }

    /**
     * Command record는 정확히 한 앱에 속한다 — 마커가 아니라 <b>유도</b>로 판정한다.
     *
     * <p>Command record에 마커를 달지 않는 이유와 유도 규칙은 {@link AppOwnership}에 적었다.
     * 0개는 고아(어느 UseCase도 쓰지 않는 죽은 코드), 2개 이상은 앱 간 공유(경계 위반)다.
     */
    @Test
    void commandRecordsShouldBelongToExactlyOneApp() {
        Map<JavaClass, Set<Class<? extends Annotation>>> apps = AppOwnership.derive(classes);

        List<String> violations = new ArrayList<>();
        apps.forEach((record, markers) -> {
            if (markers.size() != 1) {
                violations.add(record.getName() + " → 소속 앱 " + markers.size() + "개 "
                    + AppOwnership.describe(markers)
                    + (markers.isEmpty() ? " (고아 — 어느 UseCase도 쓰지 않는다)" : " (앱 간 공유)"));
            }
        });

        assertThat(violations)
            .as("Command record는 정확히 한 앱에 속한다(유도 — AppOwnership 참조)")
            .isEmpty();
    }

    /**
     * 마커별 빈 개수 anchor — 챕터 01의 앱별 anchor를 승계한다.
     *
     * <p>패키지가 평탄화돼 {@link RuleAnchorTest}가 앱별로 셀 수 없게 됐다. 합계 하한만 두면 한 앱의
     * 빈이 통째로 사라져도 나머지 세 앱이 떠받쳐 통과하므로, 마커별로 나눠 그 경우를 잡는다.
     *
     * <p>기대값은 실측(web 66·admin 62·ceo 101·batch 13)보다 낮은 하한이다 — 컨텍스트가 늘어나는
     * 것은 정상이므로 정확히 일치를 요구하면 기능 추가마다 이 파일을 고쳐야 한다.
     */
    @Test
    void markerBeanCounts() {
        assertThat(countAnnotated(WebApp.class)).as("@WebApp 빈").isGreaterThanOrEqualTo(60);
        assertThat(countAnnotated(AdminApp.class)).as("@AdminApp 빈").isGreaterThanOrEqualTo(55);
        assertThat(countAnnotated(CeoApp.class)).as("@CeoApp 빈").isGreaterThanOrEqualTo(95);
        assertThat(countAnnotated(BatchApp.class)).as("@BatchApp 빈").isGreaterThanOrEqualTo(12);
    }

    /**
     * 마커별 UseCase 개수 anchor.
     *
     * <p>batch만 <b>정확히 일치</b>다 — 잡이 7개로 규모가 작아 늘거나 줄면 의식적으로 이 숫자를
     * 고치게 되는 것이 의도이며, {@link BatchSchedulerRulesTest}의 exact anchor 방침과 같다.
     */
    @Test
    void markerUseCaseCounts() {
        assertThat(countUseCases(WebApp.class)).as("@WebApp UseCase").isGreaterThanOrEqualTo(50);
        assertThat(countUseCases(AdminApp.class)).as("@AdminApp UseCase").isGreaterThanOrEqualTo(100);
        assertThat(countUseCases(CeoApp.class)).as("@CeoApp UseCase").isGreaterThanOrEqualTo(95);
        assertThat(countUseCases(BatchApp.class)).as("@BatchApp UseCase").isEqualTo(7);
    }

    private long countAnnotated(Class<? extends Annotation> marker) {
        return classes.stream()
            .filter(c -> c.isAnnotatedWith(Service.class) || c.isAnnotatedWith(Component.class))
            .filter(c -> !c.isAnnotatedWith(Configuration.class))
            .filter(c -> c.isAnnotatedWith(marker))
            .count();
    }

    private long countUseCases(Class<? extends Annotation> marker) {
        return classes.stream()
            .filter(JavaClass::isInterface)
            .filter(c -> c.getPackageName().contains(".port.in"))
            .filter(c -> c.isAnnotatedWith(marker))
            .count();
    }

    private static ArchCondition<JavaClass> haveExactlyOneAppMarker() {
        return new ArchCondition<>("앱 마커를 정확히 1개 갖는다") {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                Set<Class<? extends Annotation>> found = AppOwnership.markersOf(item);
                boolean satisfied = found.size() == 1;
                events.add(new SimpleConditionEvent(item, satisfied,
                    item.getName() + "의 앱 마커 " + found.size() + "개 "
                        + AppOwnership.describe(found)));
            }
        };
    }
}
