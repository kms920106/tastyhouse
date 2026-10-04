package com.tastyhouse.batch;

import org.junit.jupiter.api.Test;

import com.tastyhouse.architecture.ApplicationLayerScanAssertions;
import com.tastyhouse.architecture.ModuleOrigin;

class ApplicationModuleClasspathTest {

    @Test
    void loadsOnlyOwnApplicationModule() {
        ApplicationLayerScanAssertions.assertLoadsOnlyOwnApplicationModule(ModuleOrigin.BATCH);
    }
}
