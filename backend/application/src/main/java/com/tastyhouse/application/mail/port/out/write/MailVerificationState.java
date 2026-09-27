package com.tastyhouse.application.mail.port.out.write;

import java.time.LocalDateTime;

public record MailVerificationState(
    Long id,
    String email,
    String verificationCode,
    String status,
    LocalDateTime expiresAt,
    LocalDateTime verifiedAt,
    LocalDateTime createdAt
) {
}
