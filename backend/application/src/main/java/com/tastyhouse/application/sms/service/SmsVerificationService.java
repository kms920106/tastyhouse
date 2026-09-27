package com.tastyhouse.application.sms.service;

import java.time.LocalDateTime;

import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.sms.port.out.SmsSendResult;
import com.tastyhouse.application.sms.port.out.SmsSender;
import com.tastyhouse.application.sms.port.out.write.SmsVerificationRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.vo.VerificationCode;
import com.tastyhouse.domain.sms.event.SmsVerifiedEvent;
import com.tastyhouse.domain.sms.model.SmsVerification;
import com.tastyhouse.domain.sms.model.SmsVerificationStatus;

public class SmsVerificationService {
    private final SmsVerificationRepository smsVerificationRepository;
    private final SmsSender smsSender;
    private final DomainEventPublisher domainEventPublisher;

    public SmsVerificationService(
        SmsVerificationRepository smsVerificationRepository,
        SmsSender smsSender,
        DomainEventPublisher domainEventPublisher
    ) {
        this.smsVerificationRepository = smsVerificationRepository;
        this.smsSender = smsSender;
        this.domainEventPublisher = domainEventPublisher;
    }

    public SmsVerification issue(String phoneNumber) {
        smsVerificationRepository.expireAllPendingByPhoneNumber(phoneNumber);
        SmsVerification saved = smsVerificationRepository.save(SmsVerification.create(phoneNumber));

        SmsSendResult result = smsSender.send(phoneNumber, SmsVerificationMessage.body(saved.getVerificationCode()));
        if (!result.success()) {
            throw toBusinessException(result);
        }

        return saved;
    }

    public void confirm(String phoneNumber, String verificationCode) {
        SmsVerification verification = smsVerificationRepository
            .findLatestPendingByPhoneNumber(phoneNumber, SmsVerificationStatus.PENDING)
            .orElseThrow(() -> new BusinessException(ErrorCode.SMS_VERIFICATION_CODE_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        verification.verify(VerificationCode.of(verificationCode), now);
        smsVerificationRepository.save(verification);

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
