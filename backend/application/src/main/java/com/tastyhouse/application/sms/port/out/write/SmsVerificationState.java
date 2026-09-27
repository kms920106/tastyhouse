package com.tastyhouse.application.sms.port.out.write;

import java.time.LocalDateTime;

public record SmsVerificationState(
    Long id,
    String phoneNumber,
    String verificationCode,
    String status,
    LocalDateTime expiresAt,
    LocalDateTime verifiedAt,
    LocalDateTime createdAt
) {
}
