package com.tastyhouse.batch;

import org.junit.jupiter.api.Test;

import com.tastyhouse.application.shared.marker.BatchApp;
import com.tastyhouse.architecture.ApplicationLayerScanAssertions;

class ApplicationLayerScanConfigTest {

    @Test
    void scansOnlyOwnAppAndSharedMarkers() {
        ApplicationLayerScanAssertions.assertScansOnlyOwnAppAndSharedMarkers(BatchApplication.class, BatchApp.class);
    }

    @Test
    void bootstrapShouldNotDeclareScanOrImportDirectly() {
        ApplicationLayerScanAssertions.assertBootstrapDoesNotDeclareScanOrImport(BatchApplication.class);
    }
}
