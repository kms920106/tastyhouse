package com.tastyhouse.domain.architecture;

import java.util.Set;
import java.util.TreeSet;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.assertj.core.api.Assertions.assertThat;

class ContextBoundaryTest {
    private static final String DOMAIN_ROOT = "com.tastyhouse.domain";

    private static final Set<String> NON_CONTEXT_PACKAGES = Set.of("shared", "exception");

    private static final Set<String> ALLOWED_CROSS_CONTEXT_SUBPACKAGES = Set.of("vo", "event");

    private static final Set<String> FORBIDDEN_CROSS_CONTEXT_SUBPACKAGES = Set.of("model", "repository", "service");

    private static final Set<String> SEALED_VIOLATIONS = Set.of(
        "com.tastyhouse.domain.shop.service.DeliveryAreaProjection"
    );

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages(DOMAIN_ROOT);

    @Test
    void contextsShouldNotDependOnInternalsOfOtherContexts() {
        ArchRule rule = classes()
            .that(new DescribedPredicate<>("컨텍스트에 속한다") {
                @Override
                public boolean test(JavaClass javaClass) {
                    return contextOf(javaClass.getName()) != null;
                }
            })
            .should(new com.tngtech.archunit.lang.ArchCondition<>(
                "타 컨텍스트의 model/repository/service를 import하지 않아야 한다(봉인 목록 제외)") {
                @Override
                public void check(JavaClass javaClass, ConditionEvents events) {
                    if (SEALED_VIOLATIONS.contains(topLevelNameOf(javaClass))) {
                        return;
                    }
                    for (String violation : crossContextViolationsOf(javaClass)) {
                        events.add(SimpleConditionEvent.violated(javaClass,
                            javaClass.getName() + "가 타 컨텍스트 내부를 import한다: " + violation));
                    }
                }
            })
            .because("타 컨텍스트는 ID VO(vo)·도메인 이벤트(event)로만 참조한다");

        rule.check(classes);
    }

    @Test
    void sealedViolationsShouldNotBeStale() {
        Set<String> actualViolators = new TreeSet<>();
        for (JavaClass javaClass : classes) {
            if (contextOf(javaClass.getName()) == null) {
                continue;
            }
            if (!crossContextViolationsOf(javaClass).isEmpty()) {
                actualViolators.add(topLevelNameOf(javaClass));
            }
        }

        Set<String> stale = new TreeSet<>(SEALED_VIOLATIONS);
        stale.removeAll(actualViolators);

        assertThat(stale)
            .as("봉인 목록에 있으나 더 이상 위반하지 않는 클래스 — SEALED_VIOLATIONS에서 지울 것")
            .isEmpty();
    }

    @Test
    void sealedViolationListShouldNotBeEmpty() {
        assertThat(SEALED_VIOLATIONS)
            .as("봉인 대상이 0건이면 봉인 장치(SEALED_VIOLATIONS·짝 테스트)를 제거하고 규칙을 순수 강제로 전환할 것")
            .isNotEmpty();
    }

    @Test
    void contextsShouldBeFreeOfCycles() {
        SlicesRuleDefinition.slices()
            .matching(DOMAIN_ROOT + ".(*)..")
            .namingSlices("$1")
            .should().beFreeOfCycles()
            .because("컨텍스트 간 순환은 경계를 무의미하게 만든다")
            .check(classes);
    }

    private static Set<String> crossContextViolationsOf(JavaClass javaClass) {
        String from = contextOf(javaClass.getName());
        Set<String> violations = new TreeSet<>();
        for (JavaClass target : javaClass.getDirectDependenciesFromSelf().stream()
            .map(Dependency::getTargetClass).toList()) {
            String to = contextOf(target.getName());
            if (to == null || to.equals(from)) {
                continue;
            }
            String subpackage = subpackageOf(target.getName(), to);
            if (subpackage != null && FORBIDDEN_CROSS_CONTEXT_SUBPACKAGES.contains(subpackage)) {
                violations.add(to + "." + subpackage);
            }
        }
        return violations;
    }

    private static String contextOf(String className) {
        if (!className.startsWith(DOMAIN_ROOT + ".")) {
            return null;
        }
        String remainder = className.substring(DOMAIN_ROOT.length() + 1);
        int dot = remainder.indexOf('.');
        if (dot < 0) {
            return null;
        }
        String context = remainder.substring(0, dot);
        return NON_CONTEXT_PACKAGES.contains(context) ? null : context;
    }

    private static String subpackageOf(String className, String context) {
        String remainder = className.substring(DOMAIN_ROOT.length() + context.length() + 2);

        int lastDot = remainder.lastIndexOf('.');
        if (lastDot < 0) {
            return null;
        }
        for (String segment : remainder.substring(0, lastDot).split("\\.")) {
            if (FORBIDDEN_CROSS_CONTEXT_SUBPACKAGES.contains(segment)
                || ALLOWED_CROSS_CONTEXT_SUBPACKAGES.contains(segment)) {
                return segment;
            }
        }
        return null;
    }

    private static String topLevelNameOf(JavaClass javaClass) {
        String name = javaClass.getName();
        int dollar = name.indexOf('$');
        return dollar < 0 ? name : name.substring(0, dollar);
    }
}
