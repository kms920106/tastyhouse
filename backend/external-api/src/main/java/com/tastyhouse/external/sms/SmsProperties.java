package com.tastyhouse.external.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sms")
public record SmsProperties(
    String provider,
    String senderNumber
) {
}
