package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.verification.EmailVerification;
import com.tastyhouse.core.entity.verification.EmailVerificationStatus;
import com.tastyhouse.core.repository.verification.EmailVerificationJpaRepository;
import com.tastyhouse.core.repository.verification.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationCoreService {

    private final EmailVerificationJpaRepository emailVerificationJpaRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    @Transactional
    public EmailVerification save(EmailVerification emailVerification) {
        return emailVerificationJpaRepository.save(emailVerification);
    }

    @Transactional
    public void expireAllPendingByEmail(String email) {
        emailVerificationRepository.expireAllPendingByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<EmailVerification> findLatestPendingByEmail(String email, EmailVerificationStatus status) {
        return emailVerificationRepository.findLatestPendingByEmail(email, status);
    }
}
