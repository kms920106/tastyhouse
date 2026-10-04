package com.tastyhouse.infrastructure.aws.sns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

import com.tastyhouse.application.sms.port.out.SmsSendFailure;
import com.tastyhouse.application.sms.port.out.SmsSendResult;
import com.tastyhouse.application.sms.port.out.SmsSender;

class SnsSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(SnsSmsSender.class);

    private final SnsClient snsClient;

    public SnsSmsSender(SnsClient snsClient) {
        this.snsClient = snsClient;
    }

    @Override
    public SmsSendResult send(String to, String content) {
        try {
            PublishRequest request = PublishRequest.builder()
                    .phoneNumber(to)
                    .message(content)
                    .build();

            PublishResponse response = snsClient.publish(request);
            log.info("AWS SNS SMS 발송 성공. to: {}, messageId: {}", to, response.messageId());
            return SmsSendResult.sent();
        } catch (SnsException e) {
            log.error("AWS SNS SMS 발송 실패. to: {}", to, e);
            return SmsSendResult.failed(SmsSendFailure.API_ERROR, e);
        } catch (Exception e) {
            log.error("AWS SNS SMS 발송 중 예외 발생. to: {}", to, e);
            return SmsSendResult.failed(SmsSendFailure.FAILED, e);
        }
    }
}
