package com.tastyhouse.domain.mail.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.mail.domain.vo.MailVerificationId;

public record MailVerifiedEvent(
    MailVerificationId verificationId,
    String email,
    LocalDateTime verifiedAt
) {
}
