package com.tastyhouse.external.naver.oauth;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan("com.tastyhouse.external.naver.oauth")
@EnableConfigurationProperties(NaverOAuthProperties.class)
public class NaverOAuthModuleAutoConfiguration {
}
