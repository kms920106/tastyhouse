package com.tastyhouse.external.facebook.oauth;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan("com.tastyhouse.external.facebook.oauth")
@EnableConfigurationProperties(FacebookOAuthProperties.class)
public class FacebookOAuthModuleAutoConfiguration {
}
