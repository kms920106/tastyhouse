package com.tastyhouse.batchapplication.architecture;

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

}
