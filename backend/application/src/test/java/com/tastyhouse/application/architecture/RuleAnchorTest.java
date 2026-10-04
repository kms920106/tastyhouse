package com.tastyhouse.application.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import com.tastyhouse.architecture.ModuleOrigin;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static org.assertj.core.api.Assertions.assertThat;

class RuleAnchorTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.application");

    private long countSuffix(String suffix) {
        return classes.stream().filter(c -> c.getSimpleName().endsWith(suffix)).count();
    }

    private long countBeans(String module) {
        return classes.stream()
            .filter(c -> !c.isInterface())
            .filter(c -> c.isAnnotatedWith("org.springframework.stereotype.Service")
                || c.isAnnotatedWith("org.springframework.stereotype.Component"))
            .filter(c -> ModuleOrigin.isFrom(c, module))
            .count();
    }

    private long countUseCases(String module) {
        return classes.stream()
            .filter(JavaClass::isInterface)
            .filter(c -> resideInAPackage("..port.in..").test(c))
            .filter(c -> ModuleOrigin.isFrom(c, module))
            .count();
    }

    @Test
    void moduleBeanCounts() {
        assertThat(countBeans(ModuleOrigin.WEB)).as("web-application 빈").isGreaterThanOrEqualTo(83);
        assertThat(countBeans(ModuleOrigin.ADMIN)).as("admin-application 빈").isGreaterThanOrEqualTo(65);
        assertThat(countBeans(ModuleOrigin.CEO)).as("ceo-application 빈").isGreaterThanOrEqualTo(122);
        assertThat(countBeans(ModuleOrigin.BATCH)).as("batch-application 빈").isGreaterThanOrEqualTo(15);
        assertThat(countBeans(ModuleOrigin.CORE)).as("core(application) 빈").isGreaterThanOrEqualTo(47);
    }

    @Test
    void moduleUseCaseCounts() {
        assertThat(countUseCases(ModuleOrigin.WEB)).as("web-application UseCase").isGreaterThanOrEqualTo(50);
        assertThat(countUseCases(ModuleOrigin.ADMIN)).as("admin-application UseCase").isGreaterThanOrEqualTo(100);
        assertThat(countUseCases(ModuleOrigin.CEO)).as("ceo-application UseCase").isGreaterThanOrEqualTo(95);
        assertThat(countUseCases(ModuleOrigin.BATCH)).as("batch-application UseCase").isEqualTo(7);
        assertThat(countUseCases(ModuleOrigin.CORE)).as("core에는 UseCase가 없다").isZero();
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
            .isGreaterThanOrEqualTo(1500);
    }

    @Test
    void readContractsExist() {
        assertThat(classes.stream()
            .filter(c -> resideInAPackage("..port.out..").test(c) && !resideInAPackage("..port.out.write..").test(c))
            .count())
            .as("읽기 계약이 0건이면 프레임워크-프리 규칙이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(441);
    }

    @Test
    void writePortsExist() {
        assertThat(classes.stream()
            .filter(c -> resideInAPackage("..port.out.write..").test(c))
            .filter(c -> c.isInterface() && c.getSimpleName().endsWith("Port"))
            .count())
            .as("write 포트(port.out.write의 *Port)가 0건이면 "
                + "queryServicesShouldNotDependOnWritePorts가 공허하게 통과한다")
            .isGreaterThanOrEqualTo(107);
    }

    @Test
    void testFixturesShouldNotResideInApplicationPackage() {
        assertThat(classes.stream()
            .filter(c -> c.getSource()
                .map(source -> source.getUri().toString())
                .filter(uri -> uri.contains("test-fixtures") || uri.contains("/testFixtures/"))
                .isPresent())
            .map(JavaClass::getName)
            .toList())
            .as("testFixtures 클래스가 com.tastyhouse.application 아래 있으면 DO_NOT_INCLUDE_TESTS를 통과해 "
                + "모든 application 대상 규칙의 검사 대상(프로덕션 클래스)으로 섞인다 — com.tastyhouse.architecture에 둔다")
            .isEmpty();
    }
}
