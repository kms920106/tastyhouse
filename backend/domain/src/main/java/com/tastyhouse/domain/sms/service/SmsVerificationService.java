package com.tastyhouse.domain.sms.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.sms.event.SmsVerifiedEvent;
import com.tastyhouse.domain.sms.model.SmsVerification;
import com.tastyhouse.domain.sms.model.SmsVerificationStatus;
import com.tastyhouse.domain.sms.port.SmsSender;
import com.tastyhouse.domain.sms.repository.SmsVerificationRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;
import com.tastyhouse.domain.shared.vo.VerificationCode;

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

        smsSender.send(phoneNumber, SmsVerificationMessage.body(saved.getVerificationCode()));

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
}
