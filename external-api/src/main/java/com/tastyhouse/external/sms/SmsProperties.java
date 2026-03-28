package com.tastyhouse.external.sms;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "sms")
public class SmsProperties {

    private String provider;
    private String senderNumber;
}
