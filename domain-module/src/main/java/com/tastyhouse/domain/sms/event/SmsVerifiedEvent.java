package com.tastyhouse.domain.sms.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.sms.vo.SmsVerificationId;

public record SmsVerifiedEvent(
    SmsVerificationId verificationId,
    String phoneNumber,
    LocalDateTime verifiedAt
) {
}
