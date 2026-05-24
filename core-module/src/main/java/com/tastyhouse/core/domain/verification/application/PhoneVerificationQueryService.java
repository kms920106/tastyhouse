package com.tastyhouse.core.domain.verification.application;

import com.tastyhouse.core.domain.verification.domain.model.PhoneVerification;
import com.tastyhouse.core.domain.verification.domain.model.PhoneVerificationStatus;
import com.tastyhouse.core.domain.verification.domain.repository.PhoneVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PhoneVerificationQueryService {

    private final PhoneVerificationRepository phoneVerificationRepository;

    @Transactional(readOnly = true)
    public Optional<PhoneVerification> findLatestPendingByPhoneNumber(String phoneNumber, PhoneVerificationStatus status) {
        return phoneVerificationRepository.findLatestPendingByPhoneNumber(phoneNumber, status);
    }
}
