package com.tastyhouse.external.messaging.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.shared.event.DomainEventPublisher;
import com.tastyhouse.domain.sms.port.SmsSender;
import com.tastyhouse.domain.sms.repository.SmsVerificationRepository;
import com.tastyhouse.domain.sms.service.SmsVerificationService;

@Configuration(proxyBeanMethods = false)
public class SmsDomainConfig {

    @Bean
    public SmsVerificationService smsVerificationService(
        SmsVerificationRepository smsVerificationRepository,
        SmsSender smsSender,
        DomainEventPublisher domainEventPublisher
    ) {
        return new SmsVerificationService(smsVerificationRepository, smsSender, domainEventPublisher);
    }
}
