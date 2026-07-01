package com.tastyhouse.core.domain.verification.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.verification.domain.vo.EmailVerificationId;

public record EmailVerifiedEvent(
    EmailVerificationId verificationId,
    String email,
    LocalDateTime verifiedAt
) {
}
