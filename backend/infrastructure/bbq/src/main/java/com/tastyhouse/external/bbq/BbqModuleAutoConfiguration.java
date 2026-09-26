package com.tastyhouse.external.bbq;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan(basePackages = "com.tastyhouse.external.bbq")
@EnableConfigurationProperties(BbqProperties.class)
public class BbqModuleAutoConfiguration {
}
