package com.tastyhouse.webapi;

import org.junit.jupiter.api.Test;

import com.tastyhouse.architecture.ApplicationLayerScanAssertions;

class ApplicationLayerScanConfigTest {

    @Test
    void scansApplicationLayerWithoutFilters() {
        ApplicationLayerScanAssertions.assertScansApplicationLayerWithoutFilters(WebApiApplication.class);
    }

    @Test
    void bootstrapShouldNotDeclareScanOrImportDirectly() {
        ApplicationLayerScanAssertions.assertBootstrapDoesNotDeclareScanOrImport(WebApiApplication.class);
    }

    @Test
    void scansLibraryModulesByPackageName() {
        ApplicationLayerScanAssertions.assertScansModulesWithoutFilters(
            WebApiApplication.class,
            "com.tastyhouse.infrastructure",
            "com.tastyhouse.security",
            "com.tastyhouse.logging",
            "com.tastyhouse.apicommon.ratelimit"
        );
    }

    @Test
    void libraryModulesShouldNotSelfRegisterAsAutoConfiguration() {
        ApplicationLayerScanAssertions.assertNoTastyhouseAutoConfiguration();
    }
}
