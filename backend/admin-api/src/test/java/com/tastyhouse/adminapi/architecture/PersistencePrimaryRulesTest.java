package com.tastyhouse.adminapi.architecture;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import static org.assertj.core.api.Assertions.assertThat;

class PersistencePrimaryRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.infrastructure");

    @Test
    @DisplayName("구현이 2개 이상인 아웃바운드 포트는 @Primary 구현이 정확히 1개다")
    void eachSharedOutboundPortHasExactlyOnePrimaryImplementation() {
        Map<String, List<JavaClass>> shared = new LinkedHashMap<>();
        implementationsByOutboundPort().forEach((port, implementations) -> {
            if (implementations.size() > 1) {
                shared.put(port, implementations);
            }
        });

        assertThat(shared)
            .as("JPA·MyBatis가 함께 구현하는 아웃바운드 포트가 있어야 이 가드가 공허하게 통과하지 않는다")
            .isNotEmpty();

        Map<String, List<String>> violations = new TreeMap<>();
        shared.forEach((port, implementations) -> {
            List<String> primaries = implementations.stream()
                .filter(implementation -> implementation.isAnnotatedWith(Primary.class))
                .map(JavaClass::getName)
                .toList();
            if (primaries.size() != 1) {
                violations.put(port, primaries);
            }
        });

        assertThat(violations)
            .as("아웃바운드 포트별 @Primary 구현 목록 — 정확히 1개여야 한다")
            .isEmpty();
    }

    private Map<String, List<JavaClass>> implementationsByOutboundPort() {
        Map<String, List<JavaClass>> implementationsByPort = new TreeMap<>();
        for (JavaClass javaClass : classes) {
            if (!javaClass.isAnnotatedWith(Repository.class)) {
                continue;
            }
            for (JavaClass port : javaClass.getAllRawInterfaces()) {
                if (port.getPackageName().matches("com\\.tastyhouse\\.application\\..*\\.port\\.out(\\..*)?")) {
                    implementationsByPort
                        .computeIfAbsent(port.getName(), key -> new ArrayList<>())
                        .add(javaClass);
                }
            }
        }
        return implementationsByPort;
    }
}
