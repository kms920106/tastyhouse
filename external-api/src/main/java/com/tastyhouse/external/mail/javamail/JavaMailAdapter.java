package com.tastyhouse.external.mail.javamail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.tastyhouse.domain.mail.domain.port.MailSender;
import com.tastyhouse.external.exception.ExternalApiErrorCode;
import com.tastyhouse.external.exception.ExternalApiException;
import com.tastyhouse.external.mail.MailProperties;

/**
 * JavaMail(SMTP) 기반 메일 발송 어댑터 — 도메인 포트 {@link MailSender}의 기본 구현.
 *
 * <p>클래스명이 {@code JavaMailMailSender}가 아닌 이유: 이 어댑터가 주입받는 Spring의
 * {@link JavaMailSender}와 타입명이 혼동되기 때문에 {@code Adapter} 접미어로 구분한다.
 */
@ConditionalOnProperty(name = "mail.provider", havingValue = "javamail", matchIfMissing = true)
@Component
public class JavaMailAdapter implements MailSender {

    private static final Logger log = LoggerFactory.getLogger(JavaMailAdapter.class);

    private final JavaMailSender javaMailSender;
    private final MailProperties mailProperties;

    public JavaMailAdapter(JavaMailSender javaMailSender, MailProperties mailProperties) {
        this.javaMailSender = javaMailSender;
        this.mailProperties = mailProperties;
    }

    @Override
    public void send(String to, String subject, String content) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setFrom(mailProperties.senderAddress());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, false);

            javaMailSender.send(mimeMessage);
            log.info("메일 발송 성공. to: {}, subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("메일 발송 실패. to: {}, subject: {}", to, subject, e);
            throw new ExternalApiException(ExternalApiErrorCode.MAIL_SEND_FAILED, e);
        } catch (Exception e) {
            log.error("메일 발송 중 예외 발생. to: {}, subject: {}", to, subject, e);
            throw new ExternalApiException(ExternalApiErrorCode.MAIL_SEND_FAILED, e);
        }
    }
}
