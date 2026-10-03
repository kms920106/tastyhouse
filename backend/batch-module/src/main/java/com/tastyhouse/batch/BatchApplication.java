package com.tastyhouse.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.tastyhouse.application.shared.marker.BatchApp;
import com.tastyhouse.application.shared.marker.SharedApp;

@EnableScheduling
@SpringBootApplication
public class BatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }

    @Configuration(proxyBeanMethods = false)
    @ComponentScan(
        basePackages = "com.tastyhouse.application",
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {BatchApp.class, SharedApp.class}))
    static class ApplicationLayerScanConfig {
    }
}
