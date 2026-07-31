package com.tastyhouse.domain.sms.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.sms.domain.vo.SmsVerificationId;

public record SmsVerifiedEvent(
    SmsVerificationId verificationId,
    String phoneNumber,
    LocalDateTime verifiedAt
) {
}
