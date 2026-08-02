package com.tastyhouse.domain.sms.vo;

public record SmsVerificationId(Long value) {

    public SmsVerificationId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("SmsVerificationId는 양수여야 합니다: " + value);
        }
    }

    public static SmsVerificationId of(Long value) {
        return new SmsVerificationId(value);
    }
}
