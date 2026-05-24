package com.tastyhouse.core.domain.verification.domain.repository;

import com.tastyhouse.core.domain.verification.domain.model.EmailVerification;
import com.tastyhouse.core.domain.verification.domain.model.EmailVerificationStatus;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationRepository {

    EmailVerification save(EmailVerification emailVerification);

    Optional<EmailVerification> findLatestPendingByEmail(String email, EmailVerificationStatus status);

    void expireAllPendingByEmail(String email);

    void expireAllOverdue(LocalDateTime now);
}
