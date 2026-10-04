package com.tastyhouse.infrastructure.facebook.oauth;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan("com.tastyhouse.infrastructure.facebook.oauth")
@EnableConfigurationProperties(FacebookOAuthProperties.class)
public class FacebookOAuthModuleAutoConfiguration {
}
