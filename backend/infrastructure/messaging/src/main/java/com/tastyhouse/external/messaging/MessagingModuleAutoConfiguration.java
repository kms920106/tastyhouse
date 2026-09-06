package com.tastyhouse.external.messaging;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.tastyhouse.external.mail.MailProperties;
import com.tastyhouse.external.sms.SmsProperties;
import com.tastyhouse.external.sms.solapi.SolapiProperties;

@AutoConfiguration
@ComponentScan(basePackages = {
    "com.tastyhouse.external.mail",
    "com.tastyhouse.external.sms",
    "com.tastyhouse.external.messaging"
})
@EnableConfigurationProperties({
    MailProperties.class,
    SmsProperties.class,
    SolapiProperties.class
})
public class MessagingModuleAutoConfiguration {
}
