package com.tastyhouse.external.solapi;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan("com.tastyhouse.external.solapi")
@EnableConfigurationProperties(SolapiProperties.class)
public class SolapiModuleAutoConfiguration {
}
