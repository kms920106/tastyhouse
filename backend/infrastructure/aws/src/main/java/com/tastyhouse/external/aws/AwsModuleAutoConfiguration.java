package com.tastyhouse.external.aws;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.tastyhouse.external.aws.s3.S3FileStorageProperties;

@AutoConfiguration
@ComponentScan("com.tastyhouse.external.aws")
@EnableConfigurationProperties(S3FileStorageProperties.class)
public class AwsModuleAutoConfiguration {
}
