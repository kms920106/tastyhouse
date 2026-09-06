package com.tastyhouse.external.messaging.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.mail.port.MailSender;
import com.tastyhouse.domain.mail.repository.MailVerificationRepository;
import com.tastyhouse.domain.mail.service.MailVerificationService;
import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

@Configuration(proxyBeanMethods = false)
public class MailDomainConfig {

    @Bean
    public MailVerificationService mailVerificationService(
        MemberRepository memberRepository,
        MailVerificationRepository mailVerificationRepository,
        MailSender mailSender,
        DomainEventPublisher domainEventPublisher
    ) {
        return new MailVerificationService(memberRepository, mailVerificationRepository, mailSender, domainEventPublisher);
    }
}
