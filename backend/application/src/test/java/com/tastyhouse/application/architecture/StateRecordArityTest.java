package com.tastyhouse.application.architecture;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StateRecordArityTest {

    private static final String STATE_SUFFIX = "State";
    private static final String RECONSTITUTE = "reconstitute";

    private static final Map<String, String> NON_AGGREGATE_STATES = Map.of();

    private final JavaClasses applicationClasses = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.application");

    private final JavaClasses domainClasses = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.domain");

    @Test
    @DisplayName("XxxState의 최상위 컴포넌트 수는 도메인 Xxx.reconstitute 파라미터 수와 같다")
    void stateComponentCountMatchesReconstitute() {
        List<JavaClass> states = states();
        assertThat(states)
            .as("State record가 0건이면 이 검사가 공허하게 통과한다")
            .isNotEmpty();

        List<String> violations = new ArrayList<>();
        for (JavaClass state : states) {
            if (NON_AGGREGATE_STATES.containsKey(state.getName())) {
                continue;
            }
            Optional<JavaMethod> reconstitute = reconstituteOf(state);
            if (reconstitute.isEmpty()) {
                violations.add(state.getName() + ": 대응하는 도메인 "
                    + domainSimpleName(state) + ".reconstitute를 찾지 못했다");
                continue;
            }
            int components = state.reflect().getRecordComponents().length;
            int parameters = reconstitute.get().getRawParameterTypes().size();
            if (components != parameters) {
                violations.add(state.getName() + ": 컴포넌트 " + components + "개 ≠ "
                    + reconstitute.get().getFullName() + " 파라미터 " + parameters + "개");
            }
        }

        assertThat(violations)
            .as("State 컴포넌트는 reconstitute 파라미터와 1:1이어야 한다 — 개수가 다르면 필드가 빠졌거나 합쳐졌다")
            .isEmpty();
    }

    @Test
    @DisplayName("비애그리거트 State 예외 목록은 실제 State만 가리킨다")
    void nonAggregateStatesShouldNotBeStale() {
        for (String name : NON_AGGREGATE_STATES.keySet()) {
            assertThat(applicationClasses.contain(name))
                .as("예외 목록이 낡았다 — NON_AGGREGATE_STATES에서 제거하라: " + name)
                .isTrue();
        }
    }

    private List<JavaClass> states() {
        return applicationClasses.stream()
            .filter(c -> c.getPackageName().contains(".port.out.write"))
            .filter(JavaClass::isRecord)
            .filter(c -> c.getSimpleName().endsWith(STATE_SUFFIX))
            .toList();
    }

    private Optional<JavaMethod> reconstituteOf(JavaClass state) {
        String simpleName = domainSimpleName(state);
        List<JavaMethod> candidates = domainClasses.stream()
            .filter(c -> c.getSimpleName().equals(simpleName))
            .flatMap(c -> c.getMethods().stream())
            .filter(m -> m.getName().equals(RECONSTITUTE))
            .filter(m -> m.getModifiers().contains(JavaModifier.STATIC))
            .toList();
        if (candidates.size() <= 1) {
            return candidates.stream().findFirst();
        }
        String context = contextOf(state);
        return candidates.stream()
            .filter(m -> m.getOwner().getPackageName().startsWith("com.tastyhouse.domain." + context))
            .findFirst();
    }

    private static String domainSimpleName(JavaClass state) {
        String name = state.getSimpleName();
        return name.substring(0, name.length() - STATE_SUFFIX.length());
    }

    private static String contextOf(JavaClass state) {
        String rest = state.getPackageName().substring("com.tastyhouse.application.".length());
        return rest.substring(0, rest.indexOf('.'));
    }
}
