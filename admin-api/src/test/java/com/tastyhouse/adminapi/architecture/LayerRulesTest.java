package com.tastyhouse.adminapi.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * admin-api 레이어 경계 규칙(ArchUnit).
 *
 * <p>core-module → domain-module 전환 목표 구조를 컴파일 게이트로 강제한다.
 * {@code ..application..} 패키지는 전환이 진행되며 채워지므로, 아직 클래스가 없을 때도
 * 규칙이 공허하게 통과하도록 {@code allowEmptyShould(true)}를 둔다.
 */
class LayerRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.adminapi");

    @Test
    void applicationShouldNotDependOnWebLayer() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework.web..",
                "jakarta.servlet..",
                "..request..",
                "..response.."
            )
            .allowEmptyShould(true);

        rule.check(classes);
    }

    @Test
    void controllersShouldNotDependOnRepositories() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("ApiController")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository")
            .allowEmptyShould(true);

        rule.check(classes);
    }
}
