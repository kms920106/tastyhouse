package com.tastyhouse.external.oauth.apple;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Apple id_token JWT payload claims
 *
 * - sub: Apple 사용자 고유 식별자 (앱별 고정값, pairwise)
 * - email: 실제 이메일 또는 Private Relay 주소 (@privaterelay.appleid.com)
 * - emailVerified: 항상 true (Apple은 검증된 이메일만 반환) — String 또는 Boolean
 * - isPrivateEmail: 이메일이 프라이빗 릴레이 주소인지 여부
 */
public record AppleIdTokenPayload(

    @JsonProperty("sub")
    String sub,

    @JsonProperty("email")
    String email,

    @JsonProperty("email_verified")
    Object emailVerified,

    @JsonProperty("is_private_email")
    Object isPrivateEmail
) {
    public boolean isEmailVerified() {
        if (emailVerified == null) return false;
        return "true".equals(emailVerified.toString()) || Boolean.TRUE.equals(emailVerified);
    }
}
