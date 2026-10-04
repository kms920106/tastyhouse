package com.tastyhouse.security.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
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

    @Test
    void configurationsShouldNotBePublic() {
        ArchRule rule = classes()
            .that().areAnnotatedWith("org.springframework.context.annotation.Configuration")
            .should().notBePublic()
            .because("모듈 설정 클래스는 앱 ModuleScanConfig의 문자열 스캔으로만 등록되고 어떤 클래스도 직접 참조하지 않는다");

        rule.check(classes);
    }
}
