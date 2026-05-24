package com.tastyhouse.core.domain.verification.domain.repository;

import com.tastyhouse.core.domain.verification.domain.model.PhoneVerification;
import com.tastyhouse.core.domain.verification.domain.model.PhoneVerificationStatus;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PhoneVerificationRepository {

    PhoneVerification save(PhoneVerification phoneVerification);

    Optional<PhoneVerification> findLatestPendingByPhoneNumber(String phoneNumber, PhoneVerificationStatus status);

    void expireAllPendingByPhoneNumber(String phoneNumber);

    void expireAllOverdue(LocalDateTime now);
}
