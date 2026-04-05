package com.tastyhouse.external.file.firebase;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "file.firebase")
public class FirebaseStorageProperties {

    private String serviceAccountPath;
    private String storageBucket;
    private String baseUrl;
}
