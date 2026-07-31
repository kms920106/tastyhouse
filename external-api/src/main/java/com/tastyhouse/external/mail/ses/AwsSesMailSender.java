package com.tastyhouse.external.mail.ses;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

import com.tastyhouse.domain.mail.domain.port.MailSender;
import com.tastyhouse.external.exception.ExternalApiErrorCode;
import com.tastyhouse.external.exception.ExternalApiException;

@Slf4j
@RequiredArgsConstructor
public class AwsSesMailSender implements MailSender {

    private final SesClient sesClient;
    private final String senderEmail;

    @Override
    public void send(String to, String subject, String content) {
        try {
            Body body = Body.builder()
                .text(Content.builder().charset("UTF-8").data(content).build())
                .build();

            SendEmailRequest request = SendEmailRequest.builder()
                .source(senderEmail)
                .destination(Destination.builder().toAddresses(to).build())
                .message(Message.builder()
                    .subject(Content.builder().charset("UTF-8").data(subject).build())
                    .body(body)
                    .build())
                .build();

            sesClient.sendEmail(request);
            log.info("AWS SES 메일 발송 성공. to: {}, subject: {}", to, subject);
        } catch (SesException e) {
            log.error("AWS SES 메일 발송 실패. to: {}, subject: {}", to, subject, e);
            throw new ExternalApiException(ExternalApiErrorCode.MAIL_SEND_FAILED, e);
        } catch (Exception e) {
            log.error("AWS SES 메일 발송 중 예외 발생. to: {}, subject: {}", to, subject, e);
            throw new ExternalApiException(ExternalApiErrorCode.MAIL_SEND_FAILED, e);
        }
    }
}
