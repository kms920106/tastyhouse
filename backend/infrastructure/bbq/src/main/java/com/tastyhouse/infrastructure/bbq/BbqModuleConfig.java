package com.tastyhouse.infrastructure.bbq;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BbqProperties.class)
class BbqModuleConfig {
}
