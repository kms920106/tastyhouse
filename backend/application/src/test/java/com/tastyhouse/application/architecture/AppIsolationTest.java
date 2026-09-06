package com.tastyhouse.application.architecture;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.application.shared.marker.BatchApp;
import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.application.shared.marker.WebApp;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

class AppIsolationTest {

    private static final List<Class<? extends Annotation>> MARKERS = AppOwnership.MARKERS;

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.application");

    @Test
    void appsShouldNotDependOnEachOther() {
        for (Class<? extends Annotation> from : MARKERS) {
            for (Class<? extends Annotation> to : MARKERS) {
                if (from == to) {
                    continue;
                }
                noClasses()
                    .that().areAnnotatedWith(from)
                    .should().dependOnClassesThat().areAnnotatedWith(to)
                    .because(from.getSimpleName() + "는 " + to.getSimpleName() + "에 의존하지 않는다"
                        + " — 앱이 공유하는 것은 domain과 읽기 계약뿐이다")
                    .check(classes);
            }
        }
    }

    @Test
    void beansShouldHaveExactlyOneAppMarker() {
        classes()
            .that().areAnnotatedWith(Service.class).or().areAnnotatedWith(Component.class)
            .and().areNotAnnotatedWith(Configuration.class)
            .should(haveExactlyOneAppMarker())
            .because("마커 없는 @Service는 어느 앱에도 뜨지 않는다(useDefaultFilters = false)")
            .check(classes);
    }

    @Test
    void useCasesShouldHaveExactlyOneAppMarker() {
        classes()
            .that().resideInAPackage("..port.in..").and().areInterfaces()
            .should(haveExactlyOneAppMarker())
            .because("Command record의 앱 소속은 이 마커에서 유도된다")
            .check(classes);
    }

    @Test
    void commandRecordsShouldBelongToExactlyOneApp() {
        Map<JavaClass, Set<Class<? extends Annotation>>> apps = AppOwnership.derive(classes);

        List<String> violations = new ArrayList<>();
        apps.forEach((record, markers) -> {
            if (markers.size() != 1) {
                violations.add(record.getName() + " → 소속 앱 " + markers.size() + "개 "
                    + AppOwnership.describe(markers)
                    + (markers.isEmpty() ? " (고아 — 어느 UseCase도 쓰지 않는다)" : " (앱 간 공유)"));
            }
        });

        assertThat(violations)
            .as("Command record는 정확히 한 앱에 속한다(유도 — AppOwnership 참조)")
            .isEmpty();
    }

    @Test
    void markerBeanCounts() {
        assertThat(countAnnotated(WebApp.class)).as("@WebApp 빈").isGreaterThanOrEqualTo(60);
        assertThat(countAnnotated(AdminApp.class)).as("@AdminApp 빈").isGreaterThanOrEqualTo(55);
        assertThat(countAnnotated(CeoApp.class)).as("@CeoApp 빈").isGreaterThanOrEqualTo(95);
        assertThat(countAnnotated(BatchApp.class)).as("@BatchApp 빈").isGreaterThanOrEqualTo(12);
    }

    @Test
    void markerUseCaseCounts() {
        assertThat(countUseCases(WebApp.class)).as("@WebApp UseCase").isGreaterThanOrEqualTo(50);
        assertThat(countUseCases(AdminApp.class)).as("@AdminApp UseCase").isGreaterThanOrEqualTo(100);
        assertThat(countUseCases(CeoApp.class)).as("@CeoApp UseCase").isGreaterThanOrEqualTo(95);
        assertThat(countUseCases(BatchApp.class)).as("@BatchApp UseCase").isEqualTo(7);
    }

    private long countAnnotated(Class<? extends Annotation> marker) {
        return classes.stream()
            .filter(c -> c.isAnnotatedWith(Service.class) || c.isAnnotatedWith(Component.class))
            .filter(c -> !c.isAnnotatedWith(Configuration.class))
            .filter(c -> c.isAnnotatedWith(marker))
            .count();
    }

    private long countUseCases(Class<? extends Annotation> marker) {
        return classes.stream()
            .filter(JavaClass::isInterface)
            .filter(c -> c.getPackageName().contains(".port.in"))
            .filter(c -> c.isAnnotatedWith(marker))
            .count();
    }

    private static ArchCondition<JavaClass> haveExactlyOneAppMarker() {
        return new ArchCondition<>("앱 마커를 정확히 1개 갖는다") {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                Set<Class<? extends Annotation>> found = AppOwnership.markersOf(item);
                boolean satisfied = found.size() == 1;
                events.add(new SimpleConditionEvent(item, satisfied,
                    item.getName() + "의 앱 마커 " + found.size() + "개 "
                        + AppOwnership.describe(found)));
            }
        };
    }
}
