package com.tastyhouse.adminapi.config.jwt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.tastyhouse.core.domain.admin.domain.model.Admin;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.security.jwt.TokenType;
import com.tastyhouse.security.token.BlacklistRedisRepository;
import com.tastyhouse.security.token.RefreshTokenRedisRepository;
import com.tastyhouse.adminapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.adminapi.admin.AdminQueryService;
import com.tastyhouse.adminapi.auth.response.JwtResponse;

/**
 * 관리자 토큰 발급·갱신·무효화 비즈니스 로직
 * - JwtTokenProvider: JWT 서명/파싱 전담
 * - RefreshTokenRedisRepository: Refresh Token 저장소
 * - BlacklistRedisRepository: 로그아웃된 Access Token 블랙리스트
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRedisRepository refreshTokenRepository;
    private final BlacklistRedisRepository blacklistRepository;
    private final AdminQueryService adminQueryService;

    /**
     * 로그인 성공 시 Access Token + Refresh Token 발급 및 저장
     */
    public JwtResponse issue(Authentication authentication, boolean rememberMe) {
        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication, rememberMe);

        refreshTokenRepository.save(
                authentication.getName(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenTtl(rememberMe)
        );

        return JwtResponse.of(accessToken, refreshToken, "Bearer");
    }

    /**
     * Refresh Token으로 새 Access Token + Refresh Token 재발급 (Refresh Token Rotation)
     */
    public JwtResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.ADMIN_AUTHENTICATION_FAILED, "유효하지 않은 Refresh Token입니다.");
        }
        jwtTokenProvider.validateTokenType(refreshToken, TokenType.REFRESH);

        String username = jwtTokenProvider.getUsernameFromJWT(refreshToken);

        if (refreshTokenRepository.isInvalid(username, refreshToken)) {
            throw new BusinessException(ErrorCode.ADMIN_AUTHENTICATION_FAILED, "만료되었거나 이미 로그아웃된 Refresh Token입니다.");
        }

        // 토큰 발급 이후 계정이 비활성화/삭제되었을 수 있으므로 갱신 시점에 DB로 상태를 재검증한다.
        Admin admin = adminQueryService.findByUsername(username)
            .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_AUTHENTICATION_FAILED, "존재하지 않는 관리자입니다."));
        if (!admin.isActive()) {
            refreshTokenRepository.delete(username);
            throw new BusinessException(ErrorCode.ADMIN_ACCOUNT_INACTIVE);
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

        refreshTokenRepository.save(username, newRefreshToken, jwtTokenProvider.getRefreshTokenTtl(false));

        return JwtResponse.of(newAccessToken, newRefreshToken, "Bearer");
    }

    /**
     * 로그아웃: Access Token 블랙리스트 등록 + Refresh Token 삭제
     */
    public void revoke(String bearerToken) {
        String accessToken = extractToken(bearerToken);

        if (jwtTokenProvider.validateToken(accessToken)) {
            // Refresh 토큰을 Authorization 헤더로 보내 로그아웃하는 오용 방지
            jwtTokenProvider.validateTokenType(accessToken, TokenType.ACCESS);
            blacklistRepository.add(accessToken, jwtTokenProvider.getExpirationMillis(accessToken));
            refreshTokenRepository.delete(jwtTokenProvider.getUsernameFromJWT(accessToken));
        }
    }

    private String extractToken(String bearerToken) {
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.ADMIN_AUTHENTICATION_FAILED, "유효하지 않은 토큰입니다.");
        }
        return bearerToken.substring(7).trim();
    }
}
