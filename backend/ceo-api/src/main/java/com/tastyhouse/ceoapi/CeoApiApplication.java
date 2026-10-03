package com.tastyhouse.ceoapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.application.shared.marker.SharedApp;
import com.tastyhouse.ceoapi.config.CeoSeedProperties;

@SpringBootApplication
@EnableConfigurationProperties(CeoSeedProperties.class)
public class CeoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CeoApiApplication.class, args);
    }

    @Configuration(proxyBeanMethods = false)
    @ComponentScan(
        basePackages = "com.tastyhouse.application",
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {CeoApp.class, SharedApp.class}))
    static class ApplicationLayerScanConfig {
    }
}
