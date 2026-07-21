package com.tastyhouse.core.domain.verification.domain.vo;

import java.security.SecureRandom;

public record VerificationCode(String value) {

    private static final SecureRandom RANDOM = new SecureRandom();

    public VerificationCode {
        if (value == null || !value.matches("^[0-9]{6}$")) {
            throw new IllegalArgumentException("인증코드는 6자리 숫자여야 합니다.");
        }
    }

    public static VerificationCode of(String value) {
        return new VerificationCode(value);
    }

    public static VerificationCode generate() {
        int code = RANDOM.nextInt(900000) + 100000;
        return new VerificationCode(String.valueOf(code));
    }
}
