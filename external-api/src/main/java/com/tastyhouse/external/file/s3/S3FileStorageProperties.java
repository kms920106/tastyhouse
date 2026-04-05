package com.tastyhouse.external.file.s3;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "file.aws.s3")
public class S3FileStorageProperties {

    private String bucketName;
    private String region;
    private String baseUrl;
}
