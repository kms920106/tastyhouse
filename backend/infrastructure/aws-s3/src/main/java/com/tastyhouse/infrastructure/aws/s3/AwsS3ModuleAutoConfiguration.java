package com.tastyhouse.infrastructure.aws.s3;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan("com.tastyhouse.infrastructure.aws.s3")
@EnableConfigurationProperties(S3FileStorageProperties.class)
public class AwsS3ModuleAutoConfiguration {
}
