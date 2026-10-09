package com.tastyhouse.domain.emailverification.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.emailverification.vo.MailVerificationId;

public record MailVerifiedEvent(
    MailVerificationId verificationId,
    String email,
    LocalDateTime verifiedAt
) {
}
