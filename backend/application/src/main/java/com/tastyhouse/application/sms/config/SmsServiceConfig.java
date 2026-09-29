package com.tastyhouse.application.sms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.application.sms.port.out.SmsSender;
import com.tastyhouse.application.sms.port.out.write.SmsVerificationPersistencePort;
import com.tastyhouse.application.sms.service.SmsVerificationService;

@Configuration(proxyBeanMethods = false)
@WebApp
public class SmsServiceConfig {

    @Bean
    public SmsVerificationService smsVerificationService(
        SmsVerificationPersistencePort smsVerificationPersistencePort,
        SmsSender smsSender,
        DomainEventPublisher domainEventPublisher
    ) {
        return new SmsVerificationService(smsVerificationPersistencePort, smsSender, domainEventPublisher);
    }
}
