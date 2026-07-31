package com.tastyhouse.domain.verification.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.verification.domain.vo.EmailVerificationId;

public record EmailVerifiedEvent(
    EmailVerificationId verificationId,
    String email,
    LocalDateTime verifiedAt
) {
}
