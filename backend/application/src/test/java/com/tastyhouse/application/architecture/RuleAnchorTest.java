package com.tastyhouse.application.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static org.assertj.core.api.Assertions.assertThat;

class RuleAnchorTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.application");

    private long countSuffix(String suffix) {
        return classes.stream().filter(c -> c.getSimpleName().endsWith(suffix)).count();
    }

    @Test
    void commandServicesExist() {
        assertThat(countSuffix("CommandService"))
            .as("*CommandService가 0건이면 CommandService 대상 규칙들이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(91);
    }

    @Test
    void queryServicesExist() {
        assertThat(countSuffix("QueryService"))
            .as("*QueryService가 0건이면 QueryService 대상 규칙들이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(100);
    }

    @Test
    void inboundPortsExist() {
        assertThat(classes.stream().filter(c -> resideInAPackage("..port.in..").test(c)).count())
            .as("..port.in..이 0건이면 경계 타입·web 플럼빙 규칙이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(556);
    }

    @Test
    void moduleIsNotEmpty() {
        assertThat(classes.size())
            .as("모듈이 비면 noClasses() 전역 규칙이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(852);
    }

    @Test
    void readContractsExist() {
        assertThat(classes.stream()
            .filter(c -> resideInAPackage("..port.out..").test(c))
            .count())
            .as("아웃바운드 계약이 0건이면 프레임워크-프리 규칙이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(282);
    }
}
