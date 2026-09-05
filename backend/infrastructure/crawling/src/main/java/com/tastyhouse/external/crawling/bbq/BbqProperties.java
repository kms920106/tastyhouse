package com.tastyhouse.external.crawling.bbq;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "crawling.bbq.api")
public record BbqProperties(
    String baseUrl,
    @DefaultValue("10") int timeoutSeconds
) {
}
