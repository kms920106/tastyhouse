package com.tastyhouse.architecture;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

public final class ApplicationLayerScanAssertions {

    public static final String SCAN_CONFIG_SIMPLE_NAME = "ApplicationLayerScanConfig";

    public static final String MODULE_SCAN_CONFIG_SIMPLE_NAME = "ModuleScanConfig";

    private static final String APPLICATION_BASE_PACKAGE = "com.tastyhouse.application";

    private ApplicationLayerScanAssertions() {
    }

    public static void assertScansApplicationLayerWithoutFilters(Class<?> bootstrap) {
        Class<?> scanConfig = nestedConfigOf(bootstrap, SCAN_CONFIG_SIMPLE_NAME);
        assertThat(nestedScanCarriersOf(bootstrap))
            .containsExactlyInAnyOrder(scanConfig, nestedConfigOf(bootstrap, MODULE_SCAN_CONFIG_SIMPLE_NAME));
        assertThat(Modifier.isStatic(scanConfig.getModifiers())).isTrue();
        assertThat(scanConfig.getDeclaredAnnotation(Configuration.class)).isNotNull();

        ComponentScan scan = scanConfig.getDeclaredAnnotation(ComponentScan.class);
        assertThat(scan).isNotNull();
        assertThat(scan.basePackages()).containsExactly(APPLICATION_BASE_PACKAGE);
        assertThat(scan.value()).isEmpty();
        assertThat(scan.basePackageClasses()).isEmpty();
        assertThat(scan.useDefaultFilters()).isTrue();
        assertThat(scan.includeFilters()).isEmpty();
        assertThat(scan.excludeFilters()).isEmpty();
    }

    public static void assertScansModulesWithoutFilters(Class<?> bootstrap, String... expectedPackages) {
        Class<?> moduleScanConfig = nestedConfigOf(bootstrap, MODULE_SCAN_CONFIG_SIMPLE_NAME);
        assertThat(Modifier.isStatic(moduleScanConfig.getModifiers())).isTrue();
        assertThat(moduleScanConfig.getDeclaredAnnotation(Configuration.class)).isNotNull();

        ComponentScan scan = moduleScanConfig.getDeclaredAnnotation(ComponentScan.class);
        assertThat(scan).isNotNull();
        assertThat(scan.basePackages()).containsExactly(expectedPackages);
        assertThat(scan.value()).isEmpty();
        assertThat(scan.basePackageClasses()).isEmpty();
        assertThat(scan.useDefaultFilters()).isTrue();
        assertThat(scan.includeFilters()).isEmpty();
        assertThat(scan.excludeFilters()).isEmpty();

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(MetadataReader metadataReader) {
                return metadataReader.getAnnotationMetadata().hasAnnotation(Component.class.getName())
                    || metadataReader.getAnnotationMetadata().hasMetaAnnotation(Component.class.getName());
            }
        };
        for (String basePackage : expectedPackages) {
            assertThat(scanner.findCandidateComponents(basePackage))
                .as("ModuleScanConfig의 %s가 아무 컴포넌트도 찾지 못한다 — 패키지 이름이 틀리면 그 모듈의 빈이 조용히 사라진다", basePackage)
                .isNotEmpty();
        }
    }

    public static void assertNoTastyhouseAutoConfiguration() {
        List<String> ownAutoConfigurations = ImportCandidates
            .load(AutoConfiguration.class, ApplicationLayerScanAssertions.class.getClassLoader())
            .getCandidates()
            .stream()
            .filter(candidate -> candidate.startsWith("com.tastyhouse."))
            .toList();
        assertThat(ownAutoConfigurations)
            .as("라이브러리 모듈은 AutoConfiguration.imports로 자기 등록하지 않는다 — 앱 ModuleScanConfig가 문자열 스캔으로 조립한다")
            .isEmpty();
    }

    public static void assertLoadsOnlyOwnApplicationModule(String expectedAppModule) {
        Set<String> modules = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(APPLICATION_BASE_PACKAGE)
            .stream()
            .map(ModuleOrigin::of)
            .collect(Collectors.toSet());
        assertThat(modules)
            .as("클래스패스의 application 계층은 코어와 자기 {app}-application 모듈뿐이어야 한다 — 다른 앱 모듈이 섞이면 그 앱 빈이 통째로 뜬다")
            .containsExactlyInAnyOrder(ModuleOrigin.CORE, expectedAppModule);
    }

    public static void assertBootstrapDoesNotDeclareScanOrImport(Class<?> bootstrap) {
        assertThat(bootstrap.getDeclaredAnnotation(ComponentScan.class)).isNull();
        assertThat(bootstrap.getDeclaredAnnotation(ComponentScans.class)).isNull();
        assertThat(bootstrap.getDeclaredAnnotation(Import.class)).isNull();

        SpringBootApplication springBootApplication = bootstrap.getDeclaredAnnotation(SpringBootApplication.class);
        assertThat(springBootApplication).isNotNull();
        assertThat(springBootApplication.scanBasePackages()).isEmpty();
        assertThat(springBootApplication.scanBasePackageClasses()).isEmpty();
    }

    private static List<Class<?>> nestedScanCarriersOf(Class<?> bootstrap) {
        return Arrays.stream(bootstrap.getDeclaredClasses())
            .filter(nested -> nested.getDeclaredAnnotation(ComponentScan.class) != null
                || nested.getDeclaredAnnotation(ComponentScans.class) != null)
            .toList();
    }

    private static Class<?> nestedConfigOf(Class<?> bootstrap, String simpleName) {
        return Arrays.stream(bootstrap.getDeclaredClasses())
            .filter(nested -> nested.getSimpleName().equals(simpleName))
            .findFirst()
            .orElseThrow(() -> new AssertionError(
                bootstrap.getName() + " must declare nested " + simpleName));
    }
}
