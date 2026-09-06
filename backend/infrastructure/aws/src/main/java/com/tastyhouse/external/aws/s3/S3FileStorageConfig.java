package com.tastyhouse.external.aws.s3;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "file.provider", havingValue = "s3")
public class S3FileStorageConfig {
}
