package com.tastyhouse.core.domain.verification.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.verification.domain.model.PhoneVerification;
import com.tastyhouse.core.domain.verification.domain.model.PhoneVerificationStatus;

public interface PhoneVerificationRepository {

    PhoneVerification save(PhoneVerification phoneVerification);

    Optional<PhoneVerification> findLatestPendingByPhoneNumber(String phoneNumber, PhoneVerificationStatus status);

    void expireAllPendingByPhoneNumber(String phoneNumber);
}
