package com.tastyhouse.external.bbq;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "bbq.api")
public record BbqProperties(
    String baseUrl,
    @DefaultValue("10") int timeoutSeconds
) {
}
