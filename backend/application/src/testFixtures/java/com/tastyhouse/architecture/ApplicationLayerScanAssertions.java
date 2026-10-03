package com.tastyhouse.architecture;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import com.tastyhouse.application.shared.marker.SharedApp;

import static org.assertj.core.api.Assertions.assertThat;

public final class ApplicationLayerScanAssertions {

    public static final String SCAN_CONFIG_SIMPLE_NAME = "ApplicationLayerScanConfig";

    private static final String APPLICATION_BASE_PACKAGE = "com.tastyhouse.application";

    private ApplicationLayerScanAssertions() {
    }

    public static void assertScansOnlyOwnAppAndSharedMarkers(Class<?> bootstrap, Class<? extends Annotation> appMarker) {
        assertThat(AppOwnership.MARKERS).contains(appMarker);
        assertThat(appMarker).isNotEqualTo(SharedApp.class);

        Class<?> scanConfig = scanConfigOf(bootstrap);
        assertThat(nestedScanCarriersOf(bootstrap)).containsExactly(scanConfig);
        assertThat(Modifier.isStatic(scanConfig.getModifiers())).isTrue();
        assertThat(scanConfig.getDeclaredAnnotation(Configuration.class)).isNotNull();

        ComponentScan scan = scanConfig.getDeclaredAnnotation(ComponentScan.class);
        assertThat(scan).isNotNull();
        assertThat(scan.basePackages()).containsExactly(APPLICATION_BASE_PACKAGE);
        assertThat(scan.value()).isEmpty();
        assertThat(scan.basePackageClasses()).isEmpty();
        assertThat(scan.useDefaultFilters()).isFalse();
        assertThat(scan.excludeFilters()).isEmpty();
        assertThat(scan.includeFilters()).hasSize(1);

        ComponentScan.Filter filter = scan.includeFilters()[0];
        assertThat(filter.type()).isEqualTo(FilterType.ANNOTATION);
        assertThat(filter.pattern()).isEmpty();
        assertThat(Arrays.asList(filter.classes())).containsExactlyInAnyOrder(appMarker, SharedApp.class);
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
