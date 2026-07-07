package com.tastyhouse.webapi.config.jwt.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.tastyhouse.core.domain.member.domain.model.Member;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.config.jwt.repository.BlacklistRedisRepository;
import com.tastyhouse.webapi.config.jwt.repository.RefreshTokenRedisRepository;
import com.tastyhouse.webapi.config.security.CustomUserDetails;
import com.tastyhouse.webapi.exception.UnauthorizedException;
import com.tastyhouse.webapi.auth.response.JwtResponse;

/**
 * 토큰 발급·갱신·무효화 비즈니스 로직을 담당하는 서비스
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

    /**
     * 소셜/휴대폰 로그인 등 Member 객체를 직접 사용하는 모든 로그인 경로의 단일 토큰 발급 진입점
     */
    public JwtResponse issue(Member member, boolean rememberMe) {
        CustomUserDetails userDetails = new CustomUserDetails(
            member.getId(),
            member.getUsername(),
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        return issue(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()), rememberMe);
    }

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

        return JwtResponse.of(
            accessToken,
            refreshToken,
            "Bearer"
        );
    }

    /**
     * Refresh Token으로 새 Access Token + Refresh Token 재발급 (Refresh Token Rotation)
     */
    public JwtResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("유효하지 않은 Refresh Token입니다.");
        }

        String username = jwtTokenProvider.getUsernameFromJWT(refreshToken);

        if (!refreshTokenRepository.isValid(username, refreshToken)) {
            throw new UnauthorizedException("만료되었거나 이미 로그아웃된 Refresh Token입니다.");
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

        refreshTokenRepository.save(username, newRefreshToken, jwtTokenProvider.getRefreshTokenTtl(false));

        return JwtResponse.of(
            newAccessToken,
            newRefreshToken,
            "Bearer"
        );
    }

    /**
     * 로그아웃: Access Token 블랙리스트 등록 + Refresh Token 삭제
     */
    public void revoke(String bearerToken) {
        String accessToken = extractToken(bearerToken);

        if (jwtTokenProvider.validateToken(accessToken)) {
            blacklistRepository.add(accessToken, jwtTokenProvider.getExpirationMillis(accessToken));
            refreshTokenRepository.delete(jwtTokenProvider.getUsernameFromJWT(accessToken));
        }
    }

    /**
     * Access Token만 즉시 무효화 (Refresh Token 유지)
     */
    public void invalidateAccessToken(String bearerToken) {
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            return;
        }
        String accessToken = bearerToken.substring(7).trim();

        if (jwtTokenProvider.validateToken(accessToken)) {
            blacklistRepository.add(accessToken, jwtTokenProvider.getExpirationMillis(accessToken));
        }
    }

    public boolean isBlacklisted(String accessToken) {
        return blacklistRepository.contains(accessToken);
    }

    private String extractToken(String bearerToken) {
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            throw new UnauthorizedException("유효하지 않은 토큰입니다.");
        }
        return bearerToken.substring(7).trim();
    }
}
