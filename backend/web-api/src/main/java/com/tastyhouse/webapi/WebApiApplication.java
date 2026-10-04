package com.tastyhouse.webapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class WebApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApiApplication.class, args);
    }

    @Configuration(proxyBeanMethods = false)
    @ComponentScan(basePackages = "com.tastyhouse.application")
    static class ApplicationLayerScanConfig {
    }

    @Configuration(proxyBeanMethods = false)
    @ComponentScan(basePackages = {
        "com.tastyhouse.infrastructure",
        "com.tastyhouse.security",
        "com.tastyhouse.logging",
        "com.tastyhouse.apicommon.ratelimit"
    })
    static class ModuleScanConfig {
    }
}
