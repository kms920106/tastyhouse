package com.tastyhouse.core.domain.verification.domain.event;

import com.tastyhouse.core.domain.verification.domain.vo.EmailVerificationId;

import java.time.LocalDateTime;

public record EmailVerifiedEvent(
    EmailVerificationId verificationId,
    String email,
    LocalDateTime verifiedAt
) {
}
