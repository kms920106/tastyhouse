package com.tastyhouse.core.repository.verification;

import com.tastyhouse.core.entity.verification.PhoneVerification;
import com.tastyhouse.core.entity.verification.PhoneVerificationStatus;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PhoneVerificationRepository {

    Optional<PhoneVerification> findLatestPendingByPhoneNumber(String phoneNumber, PhoneVerificationStatus status);

    void expireAllPendingByPhoneNumber(String phoneNumber);

    void expireAllOverdue(LocalDateTime now);
}
