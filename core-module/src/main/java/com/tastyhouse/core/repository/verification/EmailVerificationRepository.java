package com.tastyhouse.core.repository.verification;

import com.tastyhouse.core.entity.verification.EmailVerification;
import com.tastyhouse.core.entity.verification.EmailVerificationStatus;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationRepository {

    Optional<EmailVerification> findLatestPendingByEmail(String email, EmailVerificationStatus status);

    void expireAllPendingByEmail(String email);

    void expireAllOverdue(LocalDateTime now);
}
