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
}
