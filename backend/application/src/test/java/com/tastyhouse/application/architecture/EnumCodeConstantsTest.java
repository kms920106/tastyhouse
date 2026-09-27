package com.tastyhouse.application.architecture;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.application.payment.port.out.PgProviderCode;
import com.tastyhouse.domain.payment.model.PgProvider;

import static org.assertj.core.api.Assertions.assertThat;

class EnumCodeConstantsTest {

    private static final String CODES_SUFFIX = "Codes";

    private final JavaClasses applicationClasses = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.application");

    private final JavaClasses domainClasses = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.domain");

    @Test
    @DisplayName("PgProviderCode는 도메인 PgProvider와 상수명·순서가 같다 — 라우터가 name()으로 변환한다")
    void pgProviderCodeMatchesPgProvider() {
        assertThat(names(PgProviderCode.values())).isEqualTo(names(PgProvider.values()));
    }

    @Test
    @DisplayName("port.out의 XxxCodes 문자열 상수는 도메인 enum Xxx의 상수명과 1:1이고 값이 상수명과 같다")
    void codesMatchDomainEnums() {
        List<String> violations = new ArrayList<>();
        for (JavaClass codes : codesClasses()) {
            String enumName = codes.getSimpleName().substring(0, codes.getSimpleName().length() - CODES_SUFFIX.length());
            List<JavaClass> candidates = domainClasses.stream()
                .filter(JavaClass::isEnum)
                .filter(c -> c.getSimpleName().equals(enumName))
                .toList();
            if (candidates.size() > 1) {
                String context = contextOf(codes);
                candidates = candidates.stream()
                    .filter(c -> c.getPackageName().startsWith("com.tastyhouse.domain." + context))
                    .toList();
            }
            if (candidates.size() != 1) {
                violations.add(codes.getName() + ": 대응 도메인 enum " + enumName + "을 하나로 특정하지 못했다(" + candidates.size() + "건)");
                continue;
            }
            Set<String> enumConstants = Arrays.stream(candidates.get(0).reflect().getEnumConstants())
                .map(constant -> ((Enum<?>) constant).name())
                .collect(Collectors.toCollection(TreeSet::new));
            Set<String> codeConstants = new TreeSet<>();
            for (Field field : codes.reflect().getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers) || !Modifier.isFinal(modifiers) || field.getType() != String.class) {
                    continue;
                }
                codeConstants.add(field.getName());
                String value = readConstant(field);
                if (!field.getName().equals(value)) {
                    violations.add(codes.getName() + "." + field.getName() + " 값이 상수명과 다르다: " + value);
                }
            }
            if (!codeConstants.equals(enumConstants)) {
                violations.add(codes.getName() + ": 상수 " + codeConstants + " ≠ " + candidates.get(0).getName() + " " + enumConstants);
            }
        }

        assertThat(violations)
            .as("XxxCodes는 DB에 저장되는 enum 상수명을 persistence가 domain 없이 비교하도록 복제한 것이다 — 어긋나면 조회가 조용히 0건이 된다")
            .isEmpty();
    }

    private List<JavaClass> codesClasses() {
        return applicationClasses.stream()
            .filter(c -> c.getPackageName().contains(".port.out"))
            .filter(c -> !c.isEnum())
            .filter(c -> c.getSimpleName().endsWith(CODES_SUFFIX))
            .toList();
    }

    private static String readConstant(Field field) {
        try {
            return (String) field.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String contextOf(JavaClass codes) {
        String rest = codes.getPackageName().substring("com.tastyhouse.application.".length());
        return rest.substring(0, rest.indexOf('.'));
    }

    private static List<String> names(Enum<?>[] constants) {
        return Arrays.stream(constants).map(Enum::name).toList();
    }
}
