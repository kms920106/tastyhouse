package com.tastyhouse.external.bbq;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "bbq.api")
public class BbqProperties {

    private String baseUrl = "https://bbq.co.kr";
    private int timeoutSeconds = 10;
}
