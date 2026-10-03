package com.tastyhouse.ceoapi;

import org.junit.jupiter.api.Test;

import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.architecture.ApplicationLayerScanAssertions;

class ApplicationLayerScanConfigTest {

    @Test
    void scansOnlyOwnAppAndSharedMarkers() {
        ApplicationLayerScanAssertions.assertScansOnlyOwnAppAndSharedMarkers(CeoApiApplication.class, CeoApp.class);
    }

    @Test
    void bootstrapShouldNotDeclareScanOrImportDirectly() {
        ApplicationLayerScanAssertions.assertBootstrapDoesNotDeclareScanOrImport(CeoApiApplication.class);
    }
}
