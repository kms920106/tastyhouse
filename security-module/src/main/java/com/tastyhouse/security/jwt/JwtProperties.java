package com.tastyhouse.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 서명 시크릿과 토큰 만료 시간(ms) 설정. 값은 각 API 모듈의 application.yml이 소유하며,
 * web-api와 admin-api는 반드시 서로 다른 {@code jwt.secret}(예: JWT_SECRET vs JWT_SECRET_ADMIN)을
 * 사용해야 한다 — 동일 시크릿을 쓰면 한쪽 토큰이 다른 쪽 인증을 통과하는 권한 상승이 발생한다.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private long accessTokenExpiration;
    private long refreshTokenExpiration;
    private long rememberMeRefreshTokenExpiration;
}
