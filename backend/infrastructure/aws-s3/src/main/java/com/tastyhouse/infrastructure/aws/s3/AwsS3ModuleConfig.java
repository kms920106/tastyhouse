package com.tastyhouse.infrastructure.aws.s3;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(S3FileStorageProperties.class)
class AwsS3ModuleConfig {
}
