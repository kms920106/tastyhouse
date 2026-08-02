package com.tastyhouse.domain.mail.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.mail.vo.MailVerificationId;

public record MailVerifiedEvent(
    MailVerificationId verificationId,
    String email,
    LocalDateTime verifiedAt
) {
}
