package com.tastyhouse.application.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.assertj.core.api.Assertions.assertThat;

class AsyncRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.application");

    @Test
    void asyncListenersExistSoTheRuleIsNotVacuous() {
        long asyncClasses = classes.stream()
            .filter(javaClass -> javaClass.getMethods().stream().anyMatch(method -> method.isAnnotatedWith(Async.class)))
            .count();

        assertThat(asyncClasses).isGreaterThanOrEqualTo(3);
    }

    @Test
    void coreShouldEnableAsyncWhenAsyncMethodsExist() {
        boolean hasAsyncMethods = classes.stream()
            .anyMatch(javaClass -> javaClass.getMethods().stream().anyMatch(method -> method.isAnnotatedWith(Async.class)));

        long enableAsyncConfigurations = classes.stream()
            .filter(javaClass -> javaClass.isAnnotatedWith(Configuration.class))
            .filter(javaClass -> javaClass.isAnnotatedWith(EnableAsync.class))
            .count();

        assertThat(hasAsyncMethods).isTrue();
        assertThat(enableAsyncConfigurations)
            .as("@Async 메서드가 있으면 코어 application에 @EnableAsync @Configuration이 정확히 1개여야 한다(4앱 공통 활성화)")
            .isEqualTo(1);
    }

    @Test
    void coreEnableAsyncShouldLiveInSharedConfig() {
        ArchRule rule = classes()
            .that().areAnnotatedWith(EnableAsync.class)
            .should().resideInAPackage("com.tastyhouse.application.shared.config")
            .because("@EnableAsync는 코어가 소유한다");

        rule.check(classes);
    }
}
