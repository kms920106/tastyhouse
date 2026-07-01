package com.tastyhouse.external.email.ses;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

import com.tastyhouse.core.domain.verification.application.port.out.MailSender;
import com.tastyhouse.external.exception.ExternalApiErrorCode;
import com.tastyhouse.external.exception.ExternalApiException;

@Slf4j
@RequiredArgsConstructor
public class AwsSesEmailSender implements MailSender {

    private final SesClient sesClient;
    private final String senderEmail;

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
            Body body = isHtml
                ? Body.builder().html(Content.builder().charset("UTF-8").data(content).build()).build()
                : Body.builder().text(Content.builder().charset("UTF-8").data(content).build()).build();

            SendEmailRequest request = SendEmailRequest.builder()
                .source(senderEmail)
                .destination(Destination.builder().toAddresses(to).build())
                .message(Message.builder()
                    .subject(Content.builder().charset("UTF-8").data(subject).build())
                    .body(body)
                    .build())
                .build();

            sesClient.sendEmail(request);
            log.info("AWS SES 이메일 발송 성공. to: {}, subject: {}", to, subject);
        } catch (SesException e) {
            log.error("AWS SES 이메일 발송 실패. to: {}, subject: {}", to, subject, e);
            throw new ExternalApiException(ExternalApiErrorCode.EMAIL_SEND_FAILED, e);
        } catch (Exception e) {
            log.error("AWS SES 이메일 발송 중 예외 발생. to: {}, subject: {}", to, subject, e);
            throw new ExternalApiException(ExternalApiErrorCode.EMAIL_SEND_FAILED, e);
        }
    }
}
