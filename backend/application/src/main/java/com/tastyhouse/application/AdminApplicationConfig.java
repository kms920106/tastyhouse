package com.tastyhouse.application;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration(proxyBeanMethods = false)
@ComponentScan(
    basePackages = "com.tastyhouse.application",
    useDefaultFilters = false,
    includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = AdminApp.class))
public class AdminApplicationConfig {
}
