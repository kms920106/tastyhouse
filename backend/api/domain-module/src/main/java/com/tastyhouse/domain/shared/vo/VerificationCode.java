package com.tastyhouse.domain.shared.vo;

import java.security.SecureRandom;

/**
 * 인증코드 값 객체 (6자리 숫자).
 *
 * <p>{@code mail}·{@code sms} 두 도메인이 공유한다 — 두 채널의 코드 불변식(6자리 숫자)과
 * DB 컬럼({@code verification_code VARCHAR(6)})이 동일하므로 도메인별로 복제하지 않고
 * {@code shared/vo}에 둔다. 도메인별로 복제하면 자리수 정책이 갈릴 수 있고, 한쪽 도메인에
 * 두면 다른 도메인이 그 도메인을 참조해야 해 독립성이 깨진다({@link PhoneNumber}와 동일한 판단).
 */
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
