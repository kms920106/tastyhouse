package com.tastyhouse.webapi;

import org.junit.jupiter.api.Test;

import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.architecture.ApplicationLayerScanAssertions;

class ApplicationLayerScanConfigTest {

    @Test
    void scansOnlyOwnAppAndSharedMarkers() {
        ApplicationLayerScanAssertions.assertScansOnlyOwnAppAndSharedMarkers(WebApiApplication.class, WebApp.class);
    }

    @Test
    void bootstrapShouldNotDeclareScanOrImportDirectly() {
        ApplicationLayerScanAssertions.assertBootstrapDoesNotDeclareScanOrImport(WebApiApplication.class);
    }
}
