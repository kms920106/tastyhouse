package com.tastyhouse.application;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import com.tastyhouse.application.shared.marker.SharedApp;
import com.tastyhouse.application.shared.marker.WebApp;

@Configuration(proxyBeanMethods = false)
@ComponentScan(
    basePackages = "com.tastyhouse.application",
    useDefaultFilters = false,
    includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {WebApp.class, SharedApp.class}))
public class WebApplicationConfig {
}
