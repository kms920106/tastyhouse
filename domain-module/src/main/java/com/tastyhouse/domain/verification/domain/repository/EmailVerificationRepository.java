package com.tastyhouse.domain.verification.domain.repository;

import java.util.Optional;

import com.tastyhouse.domain.verification.domain.model.EmailVerification;
import com.tastyhouse.domain.verification.domain.model.EmailVerificationStatus;

public interface EmailVerificationRepository {

    EmailVerification save(EmailVerification emailVerification);

    Optional<EmailVerification> findLatestPendingByEmail(String email, EmailVerificationStatus status);

    void expireAllPendingByEmail(String email);
}
