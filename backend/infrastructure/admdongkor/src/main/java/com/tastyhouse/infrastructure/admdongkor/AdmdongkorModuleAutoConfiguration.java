package com.tastyhouse.infrastructure.admdongkor;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan(basePackages = "com.tastyhouse.infrastructure.admdongkor")
@EnableConfigurationProperties(AdminDongBoundaryProperties.class)
public class AdmdongkorModuleAutoConfiguration {
}
