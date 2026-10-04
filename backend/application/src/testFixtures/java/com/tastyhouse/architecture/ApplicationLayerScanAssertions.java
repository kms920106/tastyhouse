package com.tastyhouse.architecture;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

public final class ApplicationLayerScanAssertions {

    public static final String SCAN_CONFIG_SIMPLE_NAME = "ApplicationLayerScanConfig";

    private static final String APPLICATION_BASE_PACKAGE = "com.tastyhouse.application";

    private ApplicationLayerScanAssertions() {
    }

    public static void assertScansApplicationLayerWithoutFilters(Class<?> bootstrap) {
        Class<?> scanConfig = scanConfigOf(bootstrap);
        assertThat(nestedScanCarriersOf(bootstrap)).containsExactly(scanConfig);
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

    private static Class<?> scanConfigOf(Class<?> bootstrap) {
        return Arrays.stream(bootstrap.getDeclaredClasses())
            .filter(nested -> nested.getSimpleName().equals(SCAN_CONFIG_SIMPLE_NAME))
            .findFirst()
            .orElseThrow(() -> new AssertionError(
                bootstrap.getName() + " must declare nested " + SCAN_CONFIG_SIMPLE_NAME));
    }
}
