package com.tastyhouse.external.file;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file")
public record FileStorageProperties(
    String provider
) {
}
