package com.tastyhouse.infrastructure.naver.oauth;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(NaverOAuthProperties.class)
class NaverOAuthModuleConfig {
}
