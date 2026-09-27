package com.tastyhouse.external.apple.oauth;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan("com.tastyhouse.external.apple.oauth")
@EnableConfigurationProperties(AppleOAuthProperties.class)
public class AppleOAuthModuleAutoConfiguration {
}
