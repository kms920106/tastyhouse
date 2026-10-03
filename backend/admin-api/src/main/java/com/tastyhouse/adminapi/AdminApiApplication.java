package com.tastyhouse.adminapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.application.shared.marker.SharedApp;
import com.tastyhouse.adminapi.config.AdminSeedProperties;

@SpringBootApplication
@EnableConfigurationProperties(AdminSeedProperties.class)
public class AdminApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApiApplication.class, args);
    }

    @Configuration(proxyBeanMethods = false)
    @ComponentScan(
        basePackages = "com.tastyhouse.application",
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {AdminApp.class, SharedApp.class}))
    static class ApplicationLayerScanConfig {
    }
}
