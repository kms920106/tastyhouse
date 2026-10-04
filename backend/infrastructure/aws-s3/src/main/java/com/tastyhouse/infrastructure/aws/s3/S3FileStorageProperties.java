package com.tastyhouse.infrastructure.aws.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file.aws.s3")
record S3FileStorageProperties(
    String bucketName,
    String baseUrl
) {
}
