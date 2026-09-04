package com.tastyhouse.batch.architecture;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import com.tastyhouse.application.architecture.AppOwnership;
import com.tastyhouse.application.shared.marker.BatchApp;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * batch-module 레이어 경계 규칙(ArchUnit).
 *
 * <p>챕터 01로 application 계층이 batch-application으로 물리 분리된 뒤, 이 모듈에 남은 것은
 * <b>driving adapter</b>({@code <job>/adapter/in/scheduler}의 {@code @Scheduled} 트리거 7종)와
 * 부트스트랩({@code BatchApplication})뿐이다. 그래서 규칙도 "어댑터가 지켜야 할 것"만 남는다.
 *
 * <p>이동한 규칙은 batch-application의 같은 이름 테스트에 있다 —
 * {@code applicationServicesShouldNotDependOnWebLayer} · {@code shouldNotDependOnQuerydsl} ·
 * {@code requestResponseRecordsShouldBeDomainAndInfraFree}(→ {@code responseRecordsShouldBe...}) ·
 * {@code schedulerServicesShouldImplementUseCase}. 이들은 대상 클래스가 전부 이 모듈을 떠났으므로
 * 여기 남겨 두면 공허하게 통과한다.
 *
 * <p>{@code allowEmptyShould(true)}는 쓰지 않는다. 규칙이 대상을 잃으면 공허 통과를 열지 말고
 * 규칙을 지우거나(위 이동 사례) anchor를 고친다.
 */
class LayerRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.batch");

    /**
     * 어댑터는 infra persistence 어댑터에 직접 의존하지 않는다.
     *
     * <p>스케줄러가 잡 UseCase만 주입하므로 현재 위반이 없고, 트리거가 JpaRepository를 직접
     * 주입해 "잡 로직 한 줄"을 어댑터에 적는 회귀를 막는다. 대상은 이 모듈의 전 클래스라
     * 공허하지 않다.
     */
    @Test
    void shouldNotDependOnInfrastructurePersistence() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..persistence..");

        rule.check(classes);
    }

    /**
     * {@code @Scheduled} 트리거는 인바운드 포트만 주입한다(구체 서비스 금지).
     *
     * <p>챕터 01로 잡 서비스가 batch-application으로 떠나면서, 이 규칙은 "모듈 안의 구체 클래스"가
     * 아니라 <b>모듈 경계를 넘는 구체 클래스</b>를 막는 규칙이 됐다. 그래서 클래스 이름
     * ({@code *SchedulerService}) 대신 <b>패키지</b>({@code com.tastyhouse.application..service..})로
     * 대상을 잡는다 — 이렇게 해야 {@code *Executor}(예: {@code ProductSoldOutReleaseExecutor} 등)처럼
     * {@code SchedulerService}로 끝나지 않는 내부 구현까지 함께 막힌다.
     *
     * <p>정방향은 {@code ..port.in..}의 UseCase 인터페이스 주입이며, 이는 이 규칙에 걸리지 않는다.
     * 실존 스케줄러 7종에 anchor 하므로 공허하지 않다.
     */
    @Test
    void schedulersShouldDependOnUseCasesOnly() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("Scheduler")
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.application..service..")
            .because("트리거는 잡 UseCase 인터페이스만 주입한다(application 구체 서비스 금지)");

        rule.check(classes);
    }


    /**
     * <b>챕터 01 신설 · 챕터 03 재작성 — 어댑터는 자기 앱의 application 슬라이스만 의존한다.</b>
     *
     * <p><b>이 규칙은 챕터 01이 없앤 컴파일 게이트를 대체한다.</b> 그전까지 이 모듈의 어댑터가 다른
     * 앱의 UseCase를 주입하는 것은 <b>빌드가</b> 막았다 — build.gradle에 자기 앱의 application 모듈
     * 하나만 있었으므로 다른 앱의 패키지는 클래스패스에 아예 없었다. 챕터 01이 4개 application 모듈을
     * {@code :application} 하나로 합치면서 4개 앱의 클래스가 <b>전부 이 모듈의 컴파일 클래스패스에
     * 들어왔다.</b>
     *
     * <p><b>챕터 03 재작성 — 판정 근거가 패키지에서 마커로 바뀌었다.</b> 챕터 01의 원본은 자기를 뺀
     * 3개 앱 패키지를 열거해 금지했는데, 평탄화로 그 패키지들이 사라졌다. 이제 소속의 근거는
     * {@link BatchApp} 등 마커 애노테이션이므로 규칙도 마커로 판정한다.
     *
     * <p>세 갈래로 나눠 검사한다.
     * <ul>
     *   <li><b>(a) UseCase 인터페이스</b> — {@code ..port.in..}의 인터페이스에 의존한다면 그것이
     *       {@link BatchApp}를 달고 있어야 한다. 마커를 인터페이스가 직접 가지므로 술어가 단순하다.</li>
     *   <li><b>(b) Command record</b> — record에는 마커가 없다. 소속을 {@link AppOwnership#derive}로
     *       <b>유도</b>해 그 집합이 {@link BatchApp}인지 본다(유도 규칙은 그 클래스 Javadoc 참조).</li>
     *   <li><b>(c) 구체 서비스</b> — {@code @Service}/{@code @Component} 클래스 의존은 앱을 가릴 것도
     *       없이 전부 금지이며, 이미 {@code com.tastyhouse.application..service..} 패키지를 막는
     *       기존 규칙이 맡는다. 여기서 중복하지 않는다.</li>
     * </ul>
     *
     * <p>짝이 되는 규칙은 application 모듈의
     * {@code AppIsolationTest#appsShouldNotDependOnEachOther}다 — 그쪽이 application 계층끼리의 수평
     * 의존을, 이쪽이 어댑터 → 남의 application 의존을 막는다.
     */
    @Test
    void adaptersShouldOnlyUseOwnAppUseCases() {
        JavaClasses applicationClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.tastyhouse.application");

        Map<String, Set<Class<? extends Annotation>>> commandApps = new HashMap<>();
        AppOwnership.derive(applicationClasses)
            .forEach((record, apps) -> commandApps.put(record.getName(), apps));

        List<String> violations = new ArrayList<>();
        for (JavaClass adapter : classes) {
            for (JavaClass dependency : adapter.getDirectDependenciesFromSelf().stream()
                .map(Dependency::getTargetClass).toList()) {

                if (!dependency.getPackageName().contains(".port.in")) {
                    continue;
                }
                if (dependency.isInterface()) {
                    if (!dependency.isAnnotatedWith(BatchApp.class)) {
                        violations.add(adapter.getName() + " -> " + dependency.getName()
                            + " (다른 앱의 UseCase — @BatchApp가 아니다)");
                    }
                } else if (dependency.isRecord()) {
                    Set<Class<? extends Annotation>> apps = commandApps.get(dependency.getName());
                    if (apps != null && !apps.equals(Set.of(BatchApp.class))) {
                        violations.add(adapter.getName() + " -> " + dependency.getName()
                            + " (소속 앱 " + AppOwnership.describe(apps) + " — @BatchApp가 아니다)");
                    }
                }
            }
        }

        assertThat(violations)
            .as("인바운드 어댑터는 자기 앱(@BatchApp)의 application 슬라이스만 의존한다")
            .isEmpty();
    }
}
