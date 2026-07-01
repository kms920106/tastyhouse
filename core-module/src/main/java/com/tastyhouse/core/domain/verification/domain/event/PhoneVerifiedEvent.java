package com.tastyhouse.core.domain.verification.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.verification.domain.vo.PhoneVerificationId;

public record PhoneVerifiedEvent(
    PhoneVerificationId verificationId,
    String phoneNumber,
    LocalDateTime verifiedAt
) {
}
