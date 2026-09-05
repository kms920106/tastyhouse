package com.tastyhouse.external.messaging;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.external.mail.MailProperties;
import com.tastyhouse.external.sms.SmsProperties;
import com.tastyhouse.external.sms.solapi.SolapiProperties;

/**
 * infrastructure:messaging 모듈의 진입점 설정 — 메일(JavaMail)·SMS(Solapi) 발송 채널.
 *
 * <p>메일·SMS 인증은 사용자 앱에서만 쓰므로 web-api만 이 모듈을 의존한다. 분리 전에는
 * persistence의 {@code MailDomainConfig}·{@code SmsDomainConfig}가 {@code MailSender}·
 * {@code SmsSender} 빈을 무조건 요구해서 admin/ceo/batch도 발송 어댑터를 강제로 들여와야 했다.
 * 두 설정을 이 모듈로 이관해 그 결합을 끊었다 — 아웃바운드 포트 구현이 일부 앱에만 있으면
 * 그 포트를 구현하는 모듈이 도메인 서비스 빈을 등록한다(자세한 근거는 {@code AGENTS.md}).
 *
 * <p>AWS SES·SNS 구현은 {@code infrastructure:aws} 모듈에 있다.
 */
@Configuration(proxyBeanMethods = false)
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
public class MessagingModuleConfig {
}
