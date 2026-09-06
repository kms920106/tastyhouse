package com.tastyhouse.external.firebase;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan("com.tastyhouse.external.firebase")
@EnableConfigurationProperties(FirebaseStorageProperties.class)
public class FirebaseModuleAutoConfiguration {
}
