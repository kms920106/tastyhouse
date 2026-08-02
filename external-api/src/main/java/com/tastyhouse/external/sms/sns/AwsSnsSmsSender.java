package com.tastyhouse.external.sms.sns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

import com.tastyhouse.domain.sms.port.SmsSender;
import com.tastyhouse.external.exception.ExternalApiErrorCode;
import com.tastyhouse.external.exception.ExternalApiException;

public class AwsSnsSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(AwsSnsSmsSender.class);

    private final SnsClient snsClient;

    public AwsSnsSmsSender(SnsClient snsClient) {
        this.snsClient = snsClient;
    }

    @Override
    public void send(String to, String content) {
        try {
            PublishRequest request = PublishRequest.builder()
                    .phoneNumber(to)
                    .message(content)
                    .build();

            PublishResponse response = snsClient.publish(request);
            log.info("AWS SNS SMS 발송 성공. to: {}, messageId: {}", to, response.messageId());
        } catch (SnsException e) {
            log.error("AWS SNS SMS 발송 실패. to: {}", to, e);
            throw new ExternalApiException(ExternalApiErrorCode.SMS_SEND_API_ERROR, e);
        } catch (Exception e) {
            log.error("AWS SNS SMS 발송 중 예외 발생. to: {}", to, e);
            throw new ExternalApiException(ExternalApiErrorCode.SMS_SEND_FAILED, e);
        }
    }
}
