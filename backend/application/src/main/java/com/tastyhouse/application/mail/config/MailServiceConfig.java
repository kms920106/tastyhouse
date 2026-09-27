package com.tastyhouse.application.mail.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.mail.port.out.MailSender;
import com.tastyhouse.application.mail.port.out.write.MailVerificationStatePort;
import com.tastyhouse.application.mail.service.MailVerificationService;
import com.tastyhouse.application.mail.store.MailVerificationRepository;
import com.tastyhouse.application.mail.store.MailVerificationStore;
import com.tastyhouse.application.member.store.MemberRepository;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.WebApp;

@Configuration(proxyBeanMethods = false)
@WebApp
public class MailServiceConfig {

    @Bean
    public MailVerificationRepository mailVerificationRepository(MailVerificationStatePort mailVerificationStatePort) {
        return new MailVerificationStore(mailVerificationStatePort);
    }

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
