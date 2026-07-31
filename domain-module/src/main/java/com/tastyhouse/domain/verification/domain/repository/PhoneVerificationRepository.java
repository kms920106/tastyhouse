package com.tastyhouse.domain.verification.domain.repository;

import java.util.Optional;

import com.tastyhouse.domain.verification.domain.model.PhoneVerification;
import com.tastyhouse.domain.verification.domain.model.PhoneVerificationStatus;

public interface PhoneVerificationRepository {

    PhoneVerification save(PhoneVerification phoneVerification);

    Optional<PhoneVerification> findLatestPendingByPhoneNumber(String phoneNumber, PhoneVerificationStatus status);

    void expireAllPendingByPhoneNumber(String phoneNumber);
}
