package com.tastyhouse.infrastructure.admdongkor;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AdminDongBoundaryProperties.class)
class AdmdongkorModuleConfig {
}
