package com.tastyhouse.domain.phoneverification.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.phoneverification.vo.SmsVerificationId;

public record SmsVerifiedEvent(
    SmsVerificationId verificationId,
    String phoneNumber,
    LocalDateTime verifiedAt
) {
}
