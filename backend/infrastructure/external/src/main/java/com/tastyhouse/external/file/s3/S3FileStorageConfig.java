package com.tastyhouse.external.file.s3;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "file.provider", havingValue = "s3")
public class S3FileStorageConfig {
    // spring-cloud-aws-autoconfigure가 S3Operations, S3Client 빈을 자동 등록
}
