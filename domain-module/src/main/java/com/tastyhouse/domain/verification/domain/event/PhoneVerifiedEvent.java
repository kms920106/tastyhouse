package com.tastyhouse.domain.verification.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.verification.domain.vo.PhoneVerificationId;

public record PhoneVerifiedEvent(
    PhoneVerificationId verificationId,
    String phoneNumber,
    LocalDateTime verifiedAt
) {
}
