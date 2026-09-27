package com.tastyhouse.application.auth.port.out;

import java.util.function.Function;

public record SocialOAuthResult<T>(
    T value,
    SocialOAuthFailure failure
) {
    public SocialOAuthResult {
        if ((value == null) == (failure == null)) {
            throw new IllegalArgumentException("소셜 인증 결과는 값과 실패 사유 중 정확히 하나를 가져야 합니다.");
        }
    }

    public static <T> SocialOAuthResult<T> success(T value) {
        return new SocialOAuthResult<>(value, null);
    }

    public static <T> SocialOAuthResult<T> failed(SocialOAuthFailure failure) {
        return new SocialOAuthResult<>(null, failure);
    }

    public T orElseThrow(Function<SocialOAuthFailure, ? extends RuntimeException> exceptionMapper) {
        if (failure != null) {
            throw exceptionMapper.apply(failure);
        }
        return value;
    }
}
