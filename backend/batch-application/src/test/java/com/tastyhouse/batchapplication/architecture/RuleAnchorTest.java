package com.tastyhouse.batchapplication.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link LayerRulesTest}의 각 규칙이 <b>공허하게 통과하지 않음</b>을 보장한다.
 *
 * <p>이 저장소는 {@code allowEmptyShould(true)}를 금지한다 — 규칙이 대상을 잃으면 지우거나 anchor를
 * 고친다는 원칙이다. 그런데 {@code noClasses().that()...} 형태는 대상이 0건이어도 조용히 통과하므로,
 * 원칙을 지켰는지가 사람 눈에만 의존한다. 이 테스트가 그 지점을 자동화한다 — 챕터 01로 클래스가
 * 모듈 사이를 옮겨 다닌 직후라 특히 필요하다(대상이 통째로 사라져도 빌드는 green이었다).
 */
class RuleAnchorTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.batchapplication");

    /** {@code LayerRulesTest#readContractsShouldBeFrameworkFree}가 쓰는 importer와 동일 범위. */
    private final JavaClasses readContracts = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.application");

    /** {@code applicationServicesShouldNotDependOnWebLayer} · {@code schedulerServicesShouldImplementUseCase}의 anchor. */
    @Test
    void schedulerServicesExist() {
        assertThat(classes.stream()
            .filter(c -> c.getSimpleName().endsWith("SchedulerService"))
            .count())
            .as("*SchedulerService가 0건이면 두 규칙이 공허하게 통과한다")
            .isEqualTo(7);
    }

    /** {@code inboundPortsShouldBeBoundaryTyped}의 anchor. */
    @Test
    void inboundPortsExist() {
        assertThat(classes.stream()
            .filter(c -> resideInAPackage("..port.in..").test(c))
            .count())
            .as("..port.in..이 0건이면 경계 타입 규칙이 공허하게 통과한다")
            .isEqualTo(7);
    }

    /** {@code responseRecordsShouldBeDomainAndInfraFree}의 anchor. */
    @Test
    void responseRecordsExist() {
        assertThat(classes.stream()
            .filter(c -> resideInAnyPackage("..request..", "..response..").test(c))
            .count())
            .as("crawling/bbq/response record가 0건이면 record 규칙이 공허하게 통과한다")
            .isEqualTo(4);
    }

    /**
     * 모듈 전체를 대상으로 하는 규칙들({@code applicationMustBeServletFree} ·
     * {@code applicationMustNotDependOnAdapters} · {@code shouldNotDependOnQuerydsl} ·
     * {@code shouldNotDependOnInfrastructure})의 anchor.
     */
    @Test
    void moduleIsNotEmpty() {
        assertThat(classes.size())
            .as("모듈이 비면 noClasses() 전역 규칙이 전부 공허하게 통과한다")
            .isGreaterThanOrEqualTo(28);
    }

    /**
     * {@code readContractsShouldBeFrameworkFree}의 anchor(챕터 09).
     *
     * <p>이 계약들은 {@code com.tastyhouse.batchapplication}이 아니라 split package인
     * {@code com.tastyhouse.application}에 있어 위 {@code classes} importer에 잡히지 않는다.
     * 별도 anchor가 없으면 계약이 통째로 사라져도 규칙이 조용히 통과한다.
     */
    @Test
    void readContractsExist() {
        assertThat(readContracts.stream()
            .filter(c -> resideInAPackage("..port.out..").test(c))
            .filter(RuleAnchorTest::isOwnedByThisModule)
            .count())
            .as("batch 단독 읽기 계약이 0건이면 프레임워크-프리 규칙이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(2);
    }

    /**
     * 이 모듈이 <b>직접 소유</b>한 계약인지 판별한다.
     *
     * <p>이 필터가 없으면 anchor가 공허 통과를 못 막는다. {@code importPackages("com.tastyhouse.application")}은
     * 테스트 런타임 클래스패스 전체를 훑으므로 <b>{@code domain-module}의 공유 계약 55개까지 함께 세어진다</b>
     * — split package라 패키지만으로는 소유 모듈을 가릴 수 없기 때문이다. 그 55개가 하한을 떠받쳐 주면
     * 정작 이 모듈의 계약이 전부 사라져도 anchor가 통과한다(batch는 하한 2 vs 실측 모집단 57로 특히 심했다).
     *
     * <p>판별 근거는 소스 위치다 — Gradle은 프로젝트 의존을 <b>jar</b>로 올리고 자기 모듈 산출물만
     * {@code build/classes/java/main} <b>디렉터리</b>로 올리므로, jar에서 온 것을 걸러내면 자기 소유분만 남는다.
     * 하한을 "자기 몫 + 55"로 올리는 방법도 있으나, 계약 하나가 {@code domain-module}로 승격될 때마다
     * 숫자가 조용히 어긋나므로 택하지 않는다.
     */
    private static boolean isOwnedByThisModule(JavaClass contract) {
        return contract.getSource()
            .map(source -> source.getUri().toString())
            .filter(uri -> !uri.contains(".jar"))
            .isPresent();
    }

}
