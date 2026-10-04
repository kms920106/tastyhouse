package com.tastyhouse.infrastructure.firebase;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan("com.tastyhouse.infrastructure.firebase")
@EnableConfigurationProperties(FirebaseStorageProperties.class)
public class FirebaseModuleAutoConfiguration {
}
