package com.tastyhouse.external.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mail")
public record MailProperties(
    String provider,
    String senderAddress
) {
}
