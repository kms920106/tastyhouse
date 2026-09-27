package com.tastyhouse.security.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

class LayerRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.security");

    @Test
    void shouldNotDependOnDomain() {
        assertThat(classes.size())
            .as("모듈이 비면 noClasses() 전역 규칙이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(4);

        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.domain..")
            .because("표현 계층은 application만 본다(엄격 레이어드) — 401/403 계약은 ErrorContracts로 읽는다");

        rule.check(classes);
    }
}
