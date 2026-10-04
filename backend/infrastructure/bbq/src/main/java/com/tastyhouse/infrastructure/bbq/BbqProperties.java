package com.tastyhouse.infrastructure.bbq;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bbq.api")
record BbqProperties(
    String baseUrl
) {
}
