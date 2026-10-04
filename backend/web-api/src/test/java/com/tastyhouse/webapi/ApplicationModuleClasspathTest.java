package com.tastyhouse.webapi;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.tastyhouse.architecture.ApplicationLayerScanAssertions;

class ApplicationModuleClasspathTest {

    @Test
    void loadsOnlyOwnApplicationModule() throws IOException {
        ApplicationLayerScanAssertions.assertLoadsOnlyOwnApplicationModule("web");
    }
}
