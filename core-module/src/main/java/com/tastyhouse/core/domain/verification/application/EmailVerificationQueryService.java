package com.tastyhouse.core.domain.verification.application;

import com.tastyhouse.core.domain.verification.domain.model.EmailVerification;
import com.tastyhouse.core.domain.verification.domain.model.EmailVerificationStatus;
import com.tastyhouse.core.domain.verification.domain.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailVerificationQueryService {

    private final EmailVerificationRepository emailVerificationRepository;

    @Transactional(readOnly = true)
    public Optional<EmailVerification> findLatestPendingByEmail(String email, EmailVerificationStatus status) {
        return emailVerificationRepository.findLatestPendingByEmail(email, status);
    }
}
