package com.tastyhouse.application.architecture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.application.payment.port.out.PgProviderCode;

import static org.assertj.core.api.Assertions.assertThat;

class EnumCodeConstantsTest {

    private static final Set<String> ALLOWED_DOMAIN_ENUM_MIRRORS = Set.of(PgProviderCode.class.getName());

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
    @DisplayName("port.out에 도메인 enum의 복제본을 두지 않는다 — persistence가 비교할 값은 application이 도메인 enum의 name()으로 포트 인자에 넘긴다")
    void portOutShouldNotMirrorDomainEnums() {
        List<JavaClass> portOutEnums = applicationClasses.stream()
            .filter(EnumCodeConstantsTest::isEnumType)
            .filter(c -> c.getPackageName().contains(".port.out"))
            .toList();
        assertThat(portOutEnums).isNotEmpty();

        Map<Set<String>, List<String>> domainEnumsByConstants = domainClasses.stream()
            .filter(EnumCodeConstantsTest::isEnumType)
            .collect(Collectors.groupingBy(
                EnumCodeConstantsTest::constantNames,
                Collectors.mapping(JavaClass::getName, Collectors.toList())
            ));

        List<String> violations = new ArrayList<>();
        for (JavaClass portOutEnum : portOutEnums) {
            if (ALLOWED_DOMAIN_ENUM_MIRRORS.contains(portOutEnum.getName())) {
                continue;
            }
            List<String> mirrored = domainEnumsByConstants.get(constantNames(portOutEnum));
            if (mirrored != null) {
                violations.add(portOutEnum.getName() + " ≡ " + mirrored);
            }
        }

        assertThat(violations)
            .as("port.out enum이 도메인 enum과 상수 집합이 같다 — 복제본을 지우고 도메인 enum의 name()을 포트 인자로 넘긴다(의도된 벤더 계약 enum이면 허용 목록에 추가)")
            .isEmpty();
    }

    @Test
    @DisplayName("port.out에 도메인 enum 상수명을 문자열 상수로 복제하지 않는다 — 과거 XxxCodes 형태의 재발 방지")
    void portOutShouldNotDeclareDomainEnumConstantStrings() {
        Set<String> domainConstantNames = domainClasses.stream()
            .filter(EnumCodeConstantsTest::isEnumType)
            .flatMap(enumClass -> constantNames(enumClass).stream())
            .collect(Collectors.toSet());

        List<String> violations = applicationClasses.stream()
            .filter(c -> c.getPackageName().contains(".port.out"))
            .flatMap(c -> c.getFields().stream())
            .filter(field -> field.getModifiers().contains(JavaModifier.STATIC))
            .filter(field -> field.getModifiers().contains(JavaModifier.FINAL))
            .filter(field -> field.getRawType().isEquivalentTo(String.class))
            .filter(field -> domainConstantNames.contains(field.getName()))
            .map(JavaField::getFullName)
            .toList();

        assertThat(violations).isEmpty();
    }

    private static boolean isEnumType(JavaClass javaClass) {
        return javaClass.reflect().isEnum();
    }

    private static Set<String> constantNames(JavaClass enumClass) {
        return Arrays.stream(enumClass.reflect().getEnumConstants())
            .map(constant -> ((Enum<?>) constant).name())
            .collect(Collectors.toCollection(TreeSet::new));
    }

    private static List<String> names(Enum<?>[] constants) {
        return Arrays.stream(constants).map(Enum::name).toList();
    }
}
