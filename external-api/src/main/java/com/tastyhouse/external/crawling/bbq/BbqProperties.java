package com.tastyhouse.external.crawling.bbq;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "crawling.bbq.api")
public class BbqProperties {

    private String baseUrl;
    private int timeoutSeconds = 10;
}
