package com.tastyhouse.infrastructure.facebook.oauth;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(FacebookOAuthProperties.class)
class FacebookOAuthModuleConfig {
}
