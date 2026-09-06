package com.tastyhouse.external.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.tastyhouse.external.file.FileStorageProperties;

@AutoConfiguration
@ComponentScan(basePackages = {
    "com.tastyhouse.external.config",
    "com.tastyhouse.external.file"
})
@EnableConfigurationProperties(FileStorageProperties.class)
public class ExternalModuleAutoConfiguration {
}
