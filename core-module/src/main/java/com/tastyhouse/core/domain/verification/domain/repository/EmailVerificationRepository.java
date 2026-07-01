package com.tastyhouse.core.domain.verification.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.verification.domain.model.EmailVerification;
import com.tastyhouse.core.domain.verification.domain.model.EmailVerificationStatus;

public interface EmailVerificationRepository {

    EmailVerification save(EmailVerification emailVerification);

    Optional<EmailVerification> findLatestPendingByEmail(String email, EmailVerificationStatus status);

    void expireAllPendingByEmail(String email);
}
