package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.verification.PhoneVerification;
import com.tastyhouse.core.entity.verification.PhoneVerificationStatus;
import com.tastyhouse.core.repository.verification.PhoneVerificationJpaRepository;
import com.tastyhouse.core.repository.verification.PhoneVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneVerificationCoreService {

    private final PhoneVerificationJpaRepository phoneVerificationJpaRepository;
    private final PhoneVerificationRepository phoneVerificationRepository;

    @Transactional
    public PhoneVerification save(PhoneVerification phoneVerification) {
        return phoneVerificationJpaRepository.save(phoneVerification);
    }

    @Transactional
    public void expireAllPendingByPhoneNumber(String phoneNumber) {
        phoneVerificationRepository.expireAllPendingByPhoneNumber(phoneNumber);
    }

    @Transactional(readOnly = true)
    public Optional<PhoneVerification> findLatestPendingByPhoneNumber(String phoneNumber, PhoneVerificationStatus status) {
        return phoneVerificationRepository.findLatestPendingByPhoneNumber(phoneNumber, status);
    }
}
