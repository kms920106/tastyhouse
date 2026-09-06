package com.tastyhouse.application.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import com.tastyhouse.application.shared.marker.BatchApp;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

class BatchSchedulerRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.application");

    @Test
    void applicationServicesShouldNotDependOnWebLayer() {
        ArchRule rule = noClasses()
            .that().areAnnotatedWith(BatchApp.class)
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework.web.bind..",
                "org.springframework.web.servlet..",
                "org.springframework.http..",
                "jakarta.servlet.."
            )
            .because("batch 잡 서비스는 HTTP 전송 방식을 알지 않는다");

        rule.check(classes);
    }

    @Test
    void inboundPortsShouldBeBoundaryTyped() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..port.in..").and().areAnnotatedWith(BatchApp.class)
            .should().dependOnClassesThat(
                resideInAnyPackage(
                    "com.tastyhouse.domain..",
                    "com.tastyhouse.infrastructure..",
                    "org.springframework.web.."
                ).and(not(resideInAPackage("com.tastyhouse.domain.exception..")))
            )
            .because("인바운드 포트는 도메인 모델·infra·web 타입을 경계 밖으로 노출하지 않는다"
                + "(domain.exception은 횡단 관심사라 carve-out)");

        rule.check(classes);
    }

    @Test
    void schedulerServicesShouldImplementUseCase() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("SchedulerService")
            .should().implement(resideInAPackage("..port.in.."))
            .because("SchedulerService는 대응 잡 UseCase를 구현한다");

        rule.check(classes);
    }

    @Test
    void schedulerServicesExist() {
        assertThat(classes.stream()
            .filter(c -> c.isAnnotatedWith(BatchApp.class))
            .filter(c -> c.getSimpleName().endsWith("SchedulerService"))
            .count())
            .as("@BatchApp *SchedulerService가 0건이면 두 규칙이 공허하게 통과한다")
            .isEqualTo(7);
    }

    @Test
    void inboundPortsExist() {
        assertThat(classes.stream()
            .filter(JavaClass::isInterface)
            .filter(c -> c.isAnnotatedWith(BatchApp.class))
            .filter(c -> resideInAPackage("..port.in..").test(c))
            .count())
            .as("@BatchApp UseCase가 0건이면 경계 타입 규칙이 공허하게 통과한다")
            .isEqualTo(7);
    }
}
