package com.tastyhouse.infrastructure.mybatis.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class LayerRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.infrastructure.mybatis");

    @Test
    void shouldNotDependOnApiModules() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat(
                resideInAnyPackage(
                    "com.tastyhouse.webapi..",
                    "com.tastyhouse.adminapi..",
                    "com.tastyhouse.ceoapi..",
                    "com.tastyhouse.batch..",
                    "com.tastyhouse.application.."
                ).and(not(resideInAPackage("com.tastyhouse.application..port.out.."))))
            .because("infra는 application의 아웃바운드 포트(port.out)를 구현하고 유스케이스는 침범하지 않는다");

        rule.check(classes);
    }

    @Test
    void shouldNotDependOnOtherPersistenceAdapters() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.infrastructure.jpa..")
            .because("영속 어댑터 모듈끼리는 서로를 모른다 — 같은 포트의 JPA 구현과 MyBatis 구현은 @Primary로만 갈린다");

        rule.check(classes);
    }

    @Test
    void queryShouldNotDependOnDomain() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.tastyhouse.infrastructure.mybatis..query..")
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.domain..")
            .because("조회 어댑터는 domain-free 읽기 계약만 구현한다 — 도메인 모델을 쓰는 것은 영속 어댑터(XxxMyBatisPersistenceAdapter)뿐이다");

        rule.check(classes);
    }

    @Test
    void persistenceShouldNotDependOnQuery() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.tastyhouse.infrastructure.mybatis..persistence..")
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.infrastructure.mybatis..query..")
            .because("write 어댑터는 read model을 의존하지 않는다(read→write 단방향)");

        rule.check(classes);
    }
}
