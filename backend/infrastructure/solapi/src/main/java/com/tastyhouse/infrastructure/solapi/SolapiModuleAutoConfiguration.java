package com.tastyhouse.infrastructure.solapi;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan("com.tastyhouse.infrastructure.solapi")
@EnableConfigurationProperties(SolapiProperties.class)
public class SolapiModuleAutoConfiguration {
}
