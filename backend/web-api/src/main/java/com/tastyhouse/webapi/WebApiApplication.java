package com.tastyhouse.webapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import com.tastyhouse.application.shared.marker.SharedApp;
import com.tastyhouse.application.shared.marker.WebApp;

@SpringBootApplication
public class WebApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApiApplication.class, args);
    }

    @Configuration(proxyBeanMethods = false)
    @ComponentScan(
        basePackages = "com.tastyhouse.application",
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {WebApp.class, SharedApp.class}))
    static class ApplicationLayerScanConfig {
    }
}
