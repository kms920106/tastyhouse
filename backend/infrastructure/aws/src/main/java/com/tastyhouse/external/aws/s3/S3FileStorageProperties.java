package com.tastyhouse.external.aws.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file.aws.s3")
public record S3FileStorageProperties(
    String bucketName,
    String region,
    String baseUrl
) {
}
