package com.tastyhouse.external.bbq;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bbq.api")
public record BbqProperties(
    String baseUrl
) {
}
