package com.tastyhouse.application.sms.store;

import java.util.Optional;

import com.tastyhouse.domain.sms.model.SmsVerification;
import com.tastyhouse.domain.sms.model.SmsVerificationStatus;

public interface SmsVerificationRepository {
    SmsVerification save(SmsVerification smsVerification);

    Optional<SmsVerification> findLatestPendingByPhoneNumber(String phoneNumber, SmsVerificationStatus status);

    void expireAllPendingByPhoneNumber(String phoneNumber);
}
