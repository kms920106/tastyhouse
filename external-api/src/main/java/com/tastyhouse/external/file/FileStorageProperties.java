package com.tastyhouse.external.file;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageProperties {

    private String uploadPath;
    private String baseUrl;
    private String type = "local";
    private String s3BucketName;
    private String s3Region = "ap-northeast-2";
    private String firebaseServiceAccountPath;
    private String firebaseStorageBucket;
}
