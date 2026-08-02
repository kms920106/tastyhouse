package com.tastyhouse.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 서명 시크릿과 토큰 만료 시간(ms) 설정. 값은 각 API 모듈의 application.yml이 소유하며,
 * web-api와 admin-api는 반드시 서로 다른 {@code jwt.secret}(JWT_SECRET_WEB vs JWT_SECRET_ADMIN)을
 * 사용해야 한다 — 동일 시크릿을 쓰면 한쪽 토큰이 다른 쪽 인증을 통과하는 권한 상승이 발생한다.
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    String secret,
    long accessTokenExpiration,
    long refreshTokenExpiration,
    long rememberMeRefreshTokenExpiration
) {
}
