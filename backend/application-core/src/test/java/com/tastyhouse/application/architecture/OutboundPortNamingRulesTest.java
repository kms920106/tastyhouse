package com.tastyhouse.application.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.assertj.core.api.Assertions.assertThat;

class OutboundPortNamingRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.application");

    @Test
    void outboundPortInterfacesExistSoTheRuleIsNotVacuous() {
        long portInterfaces = classes.stream()
            .filter(javaClass -> javaClass.isInterface() && javaClass.getPackageName().contains(".port.out"))
            .count();

        assertThat(portInterfaces).isGreaterThanOrEqualTo(50);
    }

    @Test
    void outboundPortInterfacesShouldEndWithPort() {
        ArchRule rule = classes()
            .that().areInterfaces()
            .and().resideInAPackage("..port.out..")
            .should().haveSimpleNameEndingWith("Port")
            .because("port.out의 interface는 예외 없이 Port 접미사를 가진다");

        rule.check(classes);
    }
}
