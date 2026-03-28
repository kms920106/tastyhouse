package com.tastyhouse.external.email;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "email")
public class EmailProperties {

    private String provider;
    private String senderAddress;
}
