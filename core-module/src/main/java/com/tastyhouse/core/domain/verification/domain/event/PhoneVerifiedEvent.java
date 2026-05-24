package com.tastyhouse.core.domain.verification.domain.event;

import com.tastyhouse.core.domain.verification.domain.vo.PhoneVerificationId;

import java.time.LocalDateTime;

public record PhoneVerifiedEvent(
    PhoneVerificationId verificationId,
    String phoneNumber,
    LocalDateTime verifiedAt
) {
}
