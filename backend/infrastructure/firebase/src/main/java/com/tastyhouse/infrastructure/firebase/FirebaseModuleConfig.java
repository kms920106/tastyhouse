package com.tastyhouse.infrastructure.firebase;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(FirebaseStorageProperties.class)
public class FirebaseModuleConfig {
}
