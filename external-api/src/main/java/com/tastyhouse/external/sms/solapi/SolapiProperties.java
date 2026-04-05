package com.tastyhouse.external.sms.solapi;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "sms.solapi")
public class SolapiProperties {

    private String apiKey;
    private String apiSecret;
    private String senderNumber;
    private String baseUrl = "https://api.solapi.com";
    private String sendManyPath = "/messages/v4/send-many/detail";
}
