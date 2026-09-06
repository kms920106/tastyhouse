package com.tastyhouse.domain.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class DomainPurityTest {
    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.domain");

    @Test
    void domainShouldNotDependOnSpring() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
            .because("domain은 프레임워크-프리다(빈 등록·트랜잭션 경계는 바깥 계층 소관)");

        rule.check(classes);
    }

    @Test
    void domainShouldNotDependOnJakarta() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAnyPackage("jakarta..")
            .because("영속화·검증 매핑은 infrastructure-module의 JpaEntity가 소유한다");

        rule.check(classes);
    }

    @Test
    void domainShouldNotDependOnQuerydsl() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAnyPackage("com.querydsl..")
            .because("조회 투영은 infrastructure-module의 <ctx>/query/ DAO가 소유한다");

        rule.check(classes);
    }

    @Test
    void domainShouldNotDependOnOuterLayers() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.tastyhouse.infrastructure..",
                "com.tastyhouse.webapi..",
                "com.tastyhouse.adminapi..",
                "com.tastyhouse.ceoapi..",
                "com.tastyhouse.batch.."
            )
            .because("의존성은 항상 안쪽(domain)을 향한다 — 도메인은 포트만 선언하고 어댑터를 모른다");

        rule.check(classes);
    }
}
