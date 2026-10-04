package com.tastyhouse.adminapi;

import org.junit.jupiter.api.Test;

import com.tastyhouse.architecture.ApplicationLayerScanAssertions;

class ApplicationLayerScanConfigTest {

    @Test
    void scansApplicationLayerWithoutFilters() {
        ApplicationLayerScanAssertions.assertScansApplicationLayerWithoutFilters(AdminApiApplication.class);
    }

    @Test
    void bootstrapShouldNotDeclareScanOrImportDirectly() {
        ApplicationLayerScanAssertions.assertBootstrapDoesNotDeclareScanOrImport(AdminApiApplication.class);
    }

    @Test
    void scansLibraryModulesByPackageName() {
        ApplicationLayerScanAssertions.assertScansModulesWithoutFilters(
            AdminApiApplication.class,
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
