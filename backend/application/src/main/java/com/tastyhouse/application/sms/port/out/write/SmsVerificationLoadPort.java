package com.tastyhouse.application.sms.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.sms.model.SmsVerification;
import com.tastyhouse.domain.sms.model.SmsVerificationStatus;

public interface SmsVerificationLoadPort {

    Optional<SmsVerification> findLatestPendingByPhoneNumber(String phoneNumber, SmsVerificationStatus status);
}
