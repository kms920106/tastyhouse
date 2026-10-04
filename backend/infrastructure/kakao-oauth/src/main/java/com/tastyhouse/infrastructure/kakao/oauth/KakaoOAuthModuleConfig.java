package com.tastyhouse.infrastructure.kakao.oauth;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KakaoOAuthProperties.class)
class KakaoOAuthModuleConfig {
}
