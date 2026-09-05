package com.tastyhouse.external.messaging.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.mail.port.MailSender;
import com.tastyhouse.domain.mail.repository.MailVerificationRepository;
import com.tastyhouse.domain.mail.service.MailVerificationService;
import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

/**
 * mail 컨텍스트의 도메인 서비스(POJO) 빈 등록 설정.
 *
 * <p>도메인 서비스는 {@code @Service} 없는 순수 POJO라 Spring이 스캔할 수 없으므로,
 * 이 컨텍스트에 새 POJO 도메인 서비스를 추가하면 여기에 {@code @Bean}을 추가한다.
 */
@Configuration(proxyBeanMethods = false)
public class MailDomainConfig {

    /**
     * 메일 인증 발급·검증 규칙 — 같은 이메일의 기존 미완료 인증을 함께 만료시키는 크로스 인스턴스 불변식.
     *
     * <p>{@link MailSender}는 이 모듈(infrastructure:messaging)의 어댑터가 구현한다. 발급이 발송까지 원자적으로 수행하도록
     * 도메인 서비스에 주입한다(발송 누락 방지 — 상세는 {@code MailVerificationService} Javadoc).
     */
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
