package com.tastyhouse.application;

import com.tastyhouse.application.shared.marker.BatchApp;
import com.tastyhouse.application.shared.marker.SharedApp;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration(proxyBeanMethods = false)
@ComponentScan(
    basePackages = "com.tastyhouse.application",
    useDefaultFilters = false,
    includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {BatchApp.class, SharedApp.class}))
public class BatchApplicationConfig {
}
