package com.tastyhouse.infrastructure.solapi;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SolapiProperties.class)
public class SolapiModuleConfig {
}
