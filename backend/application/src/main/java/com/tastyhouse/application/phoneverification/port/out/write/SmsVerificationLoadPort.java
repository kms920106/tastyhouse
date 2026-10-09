package com.tastyhouse.application.phoneverification.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.phoneverification.model.SmsVerification;
import com.tastyhouse.domain.phoneverification.model.SmsVerificationStatus;

public interface SmsVerificationLoadPort {

    Optional<SmsVerification> findLatestPendingByPhoneNumber(String phoneNumber, SmsVerificationStatus status);
}
