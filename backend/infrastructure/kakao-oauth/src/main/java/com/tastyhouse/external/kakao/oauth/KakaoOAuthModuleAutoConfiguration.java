package com.tastyhouse.external.kakao.oauth;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan("com.tastyhouse.external.kakao.oauth")
@EnableConfigurationProperties(KakaoOAuthProperties.class)
public class KakaoOAuthModuleAutoConfiguration {
}
