package com.tastyhouse.external.admdongkor;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan(basePackages = "com.tastyhouse.external.admdongkor")
@EnableConfigurationProperties(AdminDongBoundaryProperties.class)
public class AdmdongkorModuleAutoConfiguration {
}
