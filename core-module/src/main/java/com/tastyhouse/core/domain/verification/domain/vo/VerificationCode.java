package com.tastyhouse.core.domain.verification.domain.vo;

import java.security.SecureRandom;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerificationCode {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Column(name = "verification_code", nullable = false, length = 6)
    private String value;

    private VerificationCode(String value) {
        if (value == null || !value.matches("^[0-9]{6}$")) {
            throw new IllegalArgumentException("인증코드는 6자리 숫자여야 합니다.");
        }
        this.value = value;
    }

    public static VerificationCode of(String value) {
        return new VerificationCode(value);
    }

    public static VerificationCode generate() {
        int code = RANDOM.nextInt(900000) + 100000;
        return new VerificationCode(String.valueOf(code));
    }
}
