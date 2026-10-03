package com.tastyhouse.adminapi;

import org.junit.jupiter.api.Test;

import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.architecture.ApplicationLayerScanAssertions;

class ApplicationLayerScanConfigTest {

    @Test
    void scansOnlyOwnAppAndSharedMarkers() {
        ApplicationLayerScanAssertions.assertScansOnlyOwnAppAndSharedMarkers(AdminApiApplication.class, AdminApp.class);
    }

    @Test
    void bootstrapShouldNotDeclareScanOrImportDirectly() {
        ApplicationLayerScanAssertions.assertBootstrapDoesNotDeclareScanOrImport(AdminApiApplication.class);
    }
}
