package com.tastyhouse.batch.architecture;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import com.tastyhouse.application.architecture.AppOwnership;
import com.tastyhouse.application.shared.marker.BatchApp;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

class LayerRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.batch");

    @Test
    void shouldNotDependOnInfrastructurePersistence() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..persistence..");

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
    void adaptersShouldOnlyUseOwnAppUseCases() {
        JavaClasses applicationClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.tastyhouse.application");

        Map<String, Set<Class<? extends Annotation>>> commandApps = new HashMap<>();
        AppOwnership.derive(applicationClasses)
            .forEach((record, apps) -> commandApps.put(record.getName(), apps));

        List<String> violations = new ArrayList<>();
        for (JavaClass adapter : classes) {
            for (JavaClass dependency : adapter.getDirectDependenciesFromSelf().stream()
                .map(Dependency::getTargetClass).toList()) {

                if (!dependency.getPackageName().contains(".port.in")) {
                    continue;
                }
                if (dependency.isInterface()) {
                    if (!dependency.isAnnotatedWith(BatchApp.class)) {
                        violations.add(adapter.getName() + " -> " + dependency.getName()
                            + " (다른 앱의 UseCase — @BatchApp가 아니다)");
                    }
                } else if (dependency.isRecord()) {
                    Set<Class<? extends Annotation>> apps = commandApps.get(dependency.getName());
                    if (apps != null && !apps.equals(Set.of(BatchApp.class))) {
                        violations.add(adapter.getName() + " -> " + dependency.getName()
                            + " (소속 앱 " + AppOwnership.describe(apps) + " — @BatchApp가 아니다)");
                    }
                }
            }
        }

        assertThat(violations)
            .as("인바운드 어댑터는 자기 앱(@BatchApp)의 application 슬라이스만 의존한다")
            .isEmpty();
    }
}
