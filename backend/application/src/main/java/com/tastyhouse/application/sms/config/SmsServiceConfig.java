package com.tastyhouse.application.sms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.application.sms.port.out.SmsSender;
import com.tastyhouse.application.sms.port.out.write.SmsVerificationStatePort;
import com.tastyhouse.application.sms.service.SmsVerificationService;
import com.tastyhouse.application.sms.store.SmsVerificationRepository;
import com.tastyhouse.application.sms.store.SmsVerificationStore;

@Configuration(proxyBeanMethods = false)
@WebApp
public class SmsServiceConfig {

    @Bean
    public SmsVerificationRepository smsVerificationRepository(SmsVerificationStatePort smsVerificationStatePort) {
        return new SmsVerificationStore(smsVerificationStatePort);
    }

    @Bean
    public SmsVerificationService smsVerificationService(
        SmsVerificationRepository smsVerificationRepository,
        SmsSender smsSender,
        DomainEventPublisher domainEventPublisher
    ) {
        return new SmsVerificationService(smsVerificationRepository, smsSender, domainEventPublisher);
    }
}
