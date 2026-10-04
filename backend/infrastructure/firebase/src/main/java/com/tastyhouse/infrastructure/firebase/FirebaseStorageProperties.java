package com.tastyhouse.infrastructure.firebase;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file.firebase")
record FirebaseStorageProperties(
    String serviceAccountJson,
    String storageBucket,
    String baseUrl
) {
}
