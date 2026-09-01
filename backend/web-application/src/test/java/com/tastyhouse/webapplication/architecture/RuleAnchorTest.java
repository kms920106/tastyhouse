package com.tastyhouse.webapplication.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link LayerRulesTest}의 각 규칙이 <b>공허하게 통과하지 않음</b>을 보장한다.
 *
 * <p>이 저장소는 {@code allowEmptyShould(true)}를 금지한다 — 규칙이 대상을 잃으면 지우거나 anchor를
 * 고친다는 원칙이다. 그런데 {@code noClasses().that()...} 형태는 대상이 0건이어도 조용히 통과하므로,
 * 원칙을 지켰는지가 사람 눈에만 의존한다. 이 테스트가 그 지점을 자동화한다 — 챕터 02로 클래스가
 * 모듈 사이를 대량으로 옮겨 다닌 직후라 특히 필요하다(대상이 통째로 사라져도 빌드는 green이었다).
 *
 * <p>기대값은 <b>하한</b>으로 둔다. 컨텍스트가 늘어나는 것은 정상이므로 정확히 일치를 요구하면
 * 기능 추가마다 이 파일을 고쳐야 하고, 그러면 anchor가 규칙이 아니라 잡음이 된다. 반대로 대량
 * 소실은 하한으로 충분히 잡힌다(batch-application은 규모가 작아 일치를 썼다).
 */
class RuleAnchorTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.webapplication");

    /** {@code LayerRulesTest#readContractsShouldBeFrameworkFree}가 쓰는 importer와 동일 범위. */
    private final JavaClasses readContracts = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.application");

    /** {@code commandServicesShouldNotDependOnQueryDaos} 등 CommandService 대상 규칙 4종의 anchor. */
    @Test
    void commandServicesExist() {
        assertThat(classes.stream()
            .filter(c -> c.getSimpleName().endsWith("CommandService"))
            .count())
            .as("*CommandService가 0건이면 CommandService 대상 규칙들이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(17);
    }

    /** {@code queryServicesShouldNotDependOnWritePorts} · {@code queryServicesShouldImplementUseCase}의 anchor. */
    @Test
    void queryServicesExist() {
        assertThat(classes.stream()
            .filter(c -> c.getSimpleName().endsWith("QueryService"))
            .count())
            .as("*QueryService가 0건이면 QueryService 대상 규칙들이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(29);
    }

    /** {@code commandRecordsShouldBeBoundaryTyped} · {@code portInShouldNotDependOnWebPlumbing} 등의 anchor. */
    @Test
    void inboundPortsExist() {
        assertThat(classes.stream()
            .filter(c -> resideInAPackage("..port.in..").test(c))
            .count())
            .as("..port.in..이 0건이면 경계 타입·web 플럼빙 규칙이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(99);
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
            .isGreaterThanOrEqualTo(297);
    }

    /**
     * {@code readContractsShouldBeFrameworkFree}의 anchor(챕터 09).
     *
     * <p>이 계약들은 {@code com.tastyhouse.webapplication}이 아니라 split package인
     * {@code com.tastyhouse.application}에 있어 위 {@code classes} importer에 잡히지 않는다.
     * 별도 anchor가 없으면 계약이 통째로 사라져도 규칙이 조용히 통과한다.
     */
    @Test
    void readContractsExist() {
        assertThat(readContracts.stream()
            .filter(c -> resideInAPackage("..port.out..").test(c))
            .count())
            .as("web 단독 읽기 계약이 0건이면 프레임워크-프리 규칙이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(85);
    }

}
