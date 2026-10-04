package com.tastyhouse.infrastructure.apple.oauth;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan("com.tastyhouse.infrastructure.apple.oauth")
@EnableConfigurationProperties(AppleOAuthProperties.class)
public class AppleOAuthModuleAutoConfiguration {
}
