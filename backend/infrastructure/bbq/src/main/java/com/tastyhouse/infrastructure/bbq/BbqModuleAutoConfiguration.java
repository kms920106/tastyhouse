package com.tastyhouse.infrastructure.bbq;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan(basePackages = "com.tastyhouse.infrastructure.bbq")
@EnableConfigurationProperties(BbqProperties.class)
public class BbqModuleAutoConfiguration {
}
