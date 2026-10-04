package com.tastyhouse.architecture;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

public final class ApplicationLayerScanAssertions {

    public static final String SCAN_CONFIG_SIMPLE_NAME = "ApplicationLayerScanConfig";

    private static final String APPLICATION_BASE_PACKAGE = "com.tastyhouse.application";

    public static final String APPLICATION_MODULE_RESOURCE = "META-INF/tastyhouse/application-module.properties";

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

    public static void assertLoadsOnlyOwnApplicationModule(String expectedApp) throws IOException {
        List<URL> resources = Collections.list(
            ApplicationLayerScanAssertions.class.getClassLoader().getResources(APPLICATION_MODULE_RESOURCE));
        assertThat(resources)
            .as("클래스패스의 {app}-application 모듈은 정확히 1개여야 한다 — 다른 앱 모듈이 섞이면 그 앱 빈이 통째로 뜬다")
            .hasSize(1);

        List<String> apps = new ArrayList<>();
        for (URL resource : resources) {
            Properties properties = new Properties();
            try (InputStream input = resource.openStream()) {
                properties.load(input);
            }
            apps.add(properties.getProperty("app"));
        }
        assertThat(apps).containsExactly(expectedApp);
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
