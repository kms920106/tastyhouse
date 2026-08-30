package com.tastyhouse.batch.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

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
     * ({@code *SchedulerService}) 대신 <b>패키지</b>({@code com.tastyhouse.batchapplication..service..})로
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
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.batchapplication..service..")
            .because("트리거는 잡 UseCase 인터페이스만 주입한다(application 구체 서비스 금지)");

        rule.check(classes);
    }

}
