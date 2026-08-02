package com.tastyhouse.external.file.firebase;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file.firebase")
public record FirebaseStorageProperties(
    String serviceAccountPath,
    String storageBucket,
    String baseUrl
) {
}
