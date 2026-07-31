package com.tastyhouse.domain.mail.domain.vo;

public record MailVerificationId(Long value) {

    public MailVerificationId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("MailVerificationId는 양수여야 합니다: " + value);
        }
    }

    public static MailVerificationId of(Long value) {
        return new MailVerificationId(value);
    }
}
