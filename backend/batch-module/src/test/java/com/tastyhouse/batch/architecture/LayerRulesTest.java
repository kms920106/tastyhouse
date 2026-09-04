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


    /**
     * <b>챕터 01 신설 — 어댑터는 자기 앱의 application 슬라이스만 의존한다.</b>
     *
     * <p><b>이 규칙은 챕터 01이 없앤 컴파일 게이트를 대체한다.</b> 그전까지 batch의 어댑터가 다른 앱의
     * UseCase를 주입하는 것은 <b>빌드가</b> 막았다 — 이 모듈의 build.gradle에
     * {@code project(':batch-application')} 하나만 있었으므로 다른 앱의 패키지는 클래스패스에 아예
     * 없었다. 챕터 01이 4개 application 모듈을 {@code :application} 하나로 합치면서 4개 앱 패키지가
     * <b>전부 이 모듈의 컴파일 클래스패스에 들어왔고</b>, 이제 batch-module이
     * {@code com.tastyhouse.webapplication..}의 UseCase를 주입해도 컴파일이 통과한다.
     *
     * <p>그래서 이 규칙을 <b>모듈 통합과 같은 커밋에</b> 넣는다. 나중에 추가하면 그 사이에 들어온
     * 교차 의존이 정상으로 굳는다.
     *
     * <p>짝이 되는 규칙은 {@code :application} 모듈의 {@code AppIsolationTest#appsShouldNotDependOnEachOther}다 —
     * 그쪽이 application 계층끼리의 수평 의존을, 이쪽이 어댑터 → 남의 application 의존을 막는다.
     * 자기 앱({@code com.tastyhouse.batchapplication..})은 정방향이므로 목록에서 제외한다.
     */
    @Test
    void adaptersShouldOnlyUseOwnAppUseCases() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.tastyhouse.webapplication..",
                "com.tastyhouse.adminapplication..",
                "com.tastyhouse.ceoapplication..")
            .because("인바운드 어댑터는 자기 앱의 application 슬라이스만 의존한다");

        rule.check(classes);
    }
}
