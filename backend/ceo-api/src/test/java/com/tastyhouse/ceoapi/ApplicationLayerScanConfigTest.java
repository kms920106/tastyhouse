package com.tastyhouse.ceoapi;

import org.junit.jupiter.api.Test;

import com.tastyhouse.architecture.ApplicationLayerScanAssertions;

class ApplicationLayerScanConfigTest {

    @Test
    void scansApplicationLayerWithoutFilters() {
        ApplicationLayerScanAssertions.assertScansApplicationLayerWithoutFilters(CeoApiApplication.class);
    }

    @Test
    void bootstrapShouldNotDeclareScanOrImportDirectly() {
        ApplicationLayerScanAssertions.assertBootstrapDoesNotDeclareScanOrImport(CeoApiApplication.class);
    }

    @Test
    void scansLibraryModulesByPackageName() {
        ApplicationLayerScanAssertions.assertScansModulesWithoutFilters(
            CeoApiApplication.class,
            "com.tastyhouse.infrastructure",
            "com.tastyhouse.security",
            "com.tastyhouse.logging",
            "com.tastyhouse.apicommon.ratelimit",
            "com.tastyhouse.apicommon.exception"
        );
    }

    @Test
    void libraryModulesShouldNotSelfRegisterAsAutoConfiguration() {
        ApplicationLayerScanAssertions.assertNoTastyhouseAutoConfiguration();
    }
}
