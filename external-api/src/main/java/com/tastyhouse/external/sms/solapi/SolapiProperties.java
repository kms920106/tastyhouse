package com.tastyhouse.external.sms.solapi;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "sms.solapi")
public record SolapiProperties(
    String apiKey,
    String apiSecret,
    String senderNumber,
    @DefaultValue("https://api.solapi.com") String baseUrl,
    @DefaultValue("/messages/v4/send-many/detail") String sendManyPath
) {
}
