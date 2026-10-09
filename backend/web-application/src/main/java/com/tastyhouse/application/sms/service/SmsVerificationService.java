package com.tastyhouse.application.sms.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.shared.vo.VerificationCode;
import com.tastyhouse.domain.sms.event.SmsVerifiedEvent;
import com.tastyhouse.domain.sms.model.SmsVerification;
import com.tastyhouse.domain.sms.model.SmsVerificationStatus;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;
import com.tastyhouse.application.sms.port.out.SmsSendResult;
import com.tastyhouse.application.sms.port.out.SmsSender;
import com.tastyhouse.application.sms.port.out.write.SmsVerificationLoadPort;
import com.tastyhouse.application.sms.port.out.write.SmsVerificationSavePort;

@Service
public class SmsVerificationService {

    private final SmsVerificationLoadPort smsVerificationLoadPort;
    private final SmsVerificationSavePort smsVerificationSavePort;
    private final SmsSender smsSender;
    private final DomainEventPublisher domainEventPublisher;

    public SmsVerificationService(
        SmsVerificationLoadPort smsVerificationLoadPort,
        SmsVerificationSavePort smsVerificationSavePort,
        SmsSender smsSender,
        DomainEventPublisher domainEventPublisher
    ) {
        this.smsVerificationLoadPort = smsVerificationLoadPort;
        this.smsVerificationSavePort = smsVerificationSavePort;
        this.smsSender = smsSender;
        this.domainEventPublisher = domainEventPublisher;
    }

    public SmsVerification issue(String phoneNumber) {
        smsVerificationSavePort.expireAllPendingByPhoneNumber(phoneNumber);
        SmsVerification saved = smsVerificationSavePort.save(SmsVerification.create(phoneNumber));

        SmsSendResult result = smsSender.send(phoneNumber, SmsVerificationMessage.body(saved.getVerificationCode()));
        if (!result.success()) {
            throw toException(result);
        }

        return saved;
    }

    public void confirm(String phoneNumber, String verificationCode) {
        SmsVerification verification = smsVerificationLoadPort
            .findLatestPendingByPhoneNumber(phoneNumber, SmsVerificationStatus.PENDING)
            .orElseThrow(() -> new ApplicationException(WebErrorCode.SMS_VERIFICATION_CODE_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        verification.verify(VerificationCode.of(verificationCode), now);
        smsVerificationSavePort.save(verification);

        domainEventPublisher.publish(new SmsVerifiedEvent(
            verification.getSmsVerificationId(),
            phoneNumber,
            now
        ));
    }

    private static ApplicationException toException(SmsSendResult result) {
        WebErrorCode errorCode = switch (result.failure()) {
            case NO_RESPONSE -> WebErrorCode.SMS_SEND_NO_RESPONSE;
            case FAILED -> WebErrorCode.SMS_SEND_FAILED;
            case API_ERROR -> WebErrorCode.SMS_SEND_API_ERROR;
        };
        return result.cause() == null
            ? new ApplicationException(errorCode)
            : new ApplicationException(errorCode, result.cause());
    }
}
