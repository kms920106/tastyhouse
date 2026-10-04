package com.tastyhouse.adminapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.adminapi.config.AdminSeedProperties;

@SpringBootApplication
@EnableConfigurationProperties(AdminSeedProperties.class)
public class AdminApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApiApplication.class, args);
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
        "com.tastyhouse.apicommon.ratelimit",
        "com.tastyhouse.apicommon.exception"
    })
    static class ModuleScanConfig {
    }
}
