package com.tastyhouse.application.mail.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.mail.port.out.MailSender;
import com.tastyhouse.application.mail.port.out.write.MailVerificationPersistencePort;
import com.tastyhouse.application.mail.service.MailVerificationService;
import com.tastyhouse.application.member.port.out.write.MemberPersistencePort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.WebApp;

@Configuration(proxyBeanMethods = false)
@WebApp
public class MailServiceConfig {

    @Bean
    public MailVerificationService mailVerificationService(
        MemberPersistencePort memberPersistencePort,
        MailVerificationPersistencePort mailVerificationPersistencePort,
        MailSender mailSender,
        DomainEventPublisher domainEventPublisher
    ) {
        return new MailVerificationService(memberPersistencePort, mailVerificationPersistencePort, mailSender, domainEventPublisher);
    }
}
