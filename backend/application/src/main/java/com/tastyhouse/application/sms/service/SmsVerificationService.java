package com.tastyhouse.application.sms.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.vo.VerificationCode;
import com.tastyhouse.domain.sms.event.SmsVerifiedEvent;
import com.tastyhouse.domain.sms.model.SmsVerification;
import com.tastyhouse.domain.sms.model.SmsVerificationStatus;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.sms.port.out.SmsSendResult;
import com.tastyhouse.application.sms.port.out.SmsSender;
import com.tastyhouse.application.sms.port.out.write.SmsVerificationPersistencePort;

public class SmsVerificationService {
    private final SmsVerificationPersistencePort smsVerificationPersistencePort;
    private final SmsSender smsSender;
    private final DomainEventPublisher domainEventPublisher;

    public SmsVerificationService(
        SmsVerificationPersistencePort smsVerificationPersistencePort,
        SmsSender smsSender,
        DomainEventPublisher domainEventPublisher
    ) {
        this.smsVerificationPersistencePort = smsVerificationPersistencePort;
        this.smsSender = smsSender;
        this.domainEventPublisher = domainEventPublisher;
    }

    public SmsVerification issue(String phoneNumber) {
        smsVerificationPersistencePort.expireAllPendingByPhoneNumber(phoneNumber);
        SmsVerification saved = smsVerificationPersistencePort.save(SmsVerification.create(phoneNumber));

        SmsSendResult result = smsSender.send(phoneNumber, SmsVerificationMessage.body(saved.getVerificationCode()));
        if (!result.success()) {
            throw toBusinessException(result);
        }

        return saved;
    }

    public void confirm(String phoneNumber, String verificationCode) {
        SmsVerification verification = smsVerificationPersistencePort
            .findLatestPendingByPhoneNumber(phoneNumber, SmsVerificationStatus.PENDING)
            .orElseThrow(() -> new BusinessException(ErrorCode.SMS_VERIFICATION_CODE_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        verification.verify(VerificationCode.of(verificationCode), now);
        smsVerificationPersistencePort.save(verification);

        domainEventPublisher.publish(new SmsVerifiedEvent(
            verification.getSmsVerificationId(),
            phoneNumber,
            now
        ));
    }

    private static BusinessException toBusinessException(SmsSendResult result) {
        ErrorCode errorCode = switch (result.failure()) {
            case NO_RESPONSE -> ErrorCode.SMS_SEND_NO_RESPONSE;
            case FAILED -> ErrorCode.SMS_SEND_FAILED;
            case API_ERROR -> ErrorCode.SMS_SEND_API_ERROR;
        };
        return result.cause() == null
            ? new BusinessException(errorCode)
            : new BusinessException(errorCode, result.cause());
    }
}
