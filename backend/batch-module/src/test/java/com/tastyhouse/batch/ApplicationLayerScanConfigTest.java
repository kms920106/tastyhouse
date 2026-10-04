package com.tastyhouse.batch;

import org.junit.jupiter.api.Test;

import com.tastyhouse.architecture.ApplicationLayerScanAssertions;

class ApplicationLayerScanConfigTest {

    @Test
    void scansApplicationLayerWithoutFilters() {
        ApplicationLayerScanAssertions.assertScansApplicationLayerWithoutFilters(BatchApplication.class);
    }

    @Test
    void bootstrapShouldNotDeclareScanOrImportDirectly() {
        ApplicationLayerScanAssertions.assertBootstrapDoesNotDeclareScanOrImport(BatchApplication.class);
    }

    @Test
    void scansLibraryModulesByPackageName() {
        ApplicationLayerScanAssertions.assertScansModulesWithoutFilters(
            BatchApplication.class,
            "com.tastyhouse.infrastructure",
            "com.tastyhouse.logging"
        );
    }

    @Test
    void libraryModulesShouldNotSelfRegisterAsAutoConfiguration() {
        ApplicationLayerScanAssertions.assertNoTastyhouseAutoConfiguration();
    }
}
