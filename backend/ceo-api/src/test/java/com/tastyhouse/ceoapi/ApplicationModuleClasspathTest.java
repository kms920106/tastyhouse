package com.tastyhouse.ceoapi;

import org.junit.jupiter.api.Test;

import com.tastyhouse.architecture.ApplicationLayerScanAssertions;
import com.tastyhouse.architecture.ModuleOrigin;

class ApplicationModuleClasspathTest {

    @Test
    void loadsOnlyOwnApplicationModule() {
        ApplicationLayerScanAssertions.assertLoadsOnlyOwnApplicationModule(ModuleOrigin.CEO);
    }
}
