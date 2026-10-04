package com.tastyhouse.batch.architecture;

import java.util.List;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

class LayerRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.batch");

    @Test
    void shouldNotDependOnInfrastructurePersistence() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.infrastructure.persistence..");

        rule.check(classes);
    }

    @Test
    void schedulersShouldDependOnUseCasesOnly() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("Scheduler")
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.application..service..")
            .because("트리거는 잡 UseCase 인터페이스만 주입한다(application 구체 서비스 금지)");

        rule.check(classes);
    }

    @Test
    void useCaseFieldsShouldBeNamedUseCase() {
        List<JavaField> useCaseFields = classes.stream()
            .flatMap(javaClass -> javaClass.getFields().stream())
            .filter(field -> isPortInInterface(field.getRawType()))
            .toList();

        assertThat(useCaseFields)
            .as("UseCase 타입 필드가 줄면 타입 해석이 깨져 이 규칙이 공허하게 통과할 수 있다")
            .hasSizeGreaterThanOrEqualTo(5);

        List<String> violations = useCaseFields.stream()
            .filter(field -> !field.getName().endsWith("UseCase"))
            .map(JavaField::getFullName)
            .toList();

        assertThat(violations)
            .as("port.in 인터페이스 타입 필드는 이름이 UseCase로 끝난다(변수명은 타입을 따른다)")
            .isEmpty();
    }

    private static boolean isPortInInterface(JavaClass javaClass) {
        String packageName = javaClass.getPackageName();
        return javaClass.isInterface()
            && (packageName.endsWith(".port.in") || packageName.contains(".port.in."));
    }
}
