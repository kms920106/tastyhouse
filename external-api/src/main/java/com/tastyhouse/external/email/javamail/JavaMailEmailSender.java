package com.tastyhouse.external.email.javamail;

import com.tastyhouse.external.email.EmailProperties;
import com.tastyhouse.external.email.EmailSender;
import com.tastyhouse.external.exception.ExternalApiErrorCode;
import com.tastyhouse.external.exception.ExternalApiException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@ConditionalOnProperty(name = "email.provider", havingValue = "javamail", matchIfMissing = true)
@Component
@RequiredArgsConstructor
public class JavaMailEmailSender implements EmailSender {

    private final JavaMailSender javaMailSender;
    private final EmailProperties emailProperties;

    @Override
    public void send(String to, String subject, String content) {
        sendEmail(to, subject, content, false);
    }

    @Override
    public void sendHtml(String to, String subject, String htmlContent) {
        sendEmail(to, subject, htmlContent, true);
    }

    private void sendEmail(String to, String subject, String content, boolean isHtml) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setFrom(emailProperties.getSenderAddress());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, isHtml);

            javaMailSender.send(mimeMessage);
            log.info("이메일 발송 성공. to: {}, subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("이메일 발송 실패. to: {}, subject: {}", to, subject, e);
            throw new ExternalApiException(ExternalApiErrorCode.EMAIL_SEND_FAILED, e);
        } catch (Exception e) {
            log.error("이메일 발송 중 예외 발생. to: {}, subject: {}", to, subject, e);
            throw new ExternalApiException(ExternalApiErrorCode.EMAIL_SEND_FAILED, e);
        }
    }
}
