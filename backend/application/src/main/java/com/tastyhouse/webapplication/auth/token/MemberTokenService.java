package com.tastyhouse.webapplication.auth.token;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.security.token.BlacklistRedisRepository;
import com.tastyhouse.security.token.RefreshTokenRedisRepository;
import com.tastyhouse.webapplication.auth.security.MemberUserDetails;
import com.tastyhouse.webapplication.auth.port.out.MemberJwtResult;

/**
 * 토큰 발급·갱신·무효화 비즈니스 로직을 담당하는 서비스
 * - MemberJwtTokenProvider: JWT 서명/파싱 전담
 * - RefreshTokenRedisRepository: Refresh Token 저장소
 * - BlacklistRedisRepository: 로그아웃된 Access Token 블랙리스트
 */
@Service
public class MemberTokenService {

    private final MemberJwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRedisRepository refreshTokenRepository;
    private final BlacklistRedisRepository blacklistRepository;

    public MemberTokenService(
        MemberJwtTokenProvider jwtTokenProvider,
        RefreshTokenRedisRepository refreshTokenRepository,
        BlacklistRedisRepository blacklistRepository
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.blacklistRepository = blacklistRepository;
    }

    /**
     * 소셜/휴대폰 로그인 등 Member 객체를 직접 사용하는 모든 로그인 경로의 단일 토큰 발급 진입점
     */
    public MemberJwtResult issue(Member member, boolean rememberMe) {
        MemberUserDetails userDetails = new MemberUserDetails(
            member.getId(),
            member.getUsername(),
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        return issue(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()), rememberMe);
    }

    /**
     * 로그인 성공 시 Access Token + Refresh Token 발급 및 저장
     */
    public MemberJwtResult issue(Authentication authentication, boolean rememberMe) {
        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication, rememberMe);

        refreshTokenRepository.save(
                authentication.getName(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenTtl(rememberMe)
        );

        return MemberJwtResult.of(
            accessToken,
            refreshToken,
            "Bearer"
        );
    }

    /**
     * Refresh Token으로 새 Access Token + Refresh Token 재발급 (Refresh Token Rotation)
     */
    public MemberJwtResult refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_INVALID);
        }

        String username = jwtTokenProvider.getUsernameFromJWT(refreshToken);

        if (refreshTokenRepository.isInvalid(username, refreshToken)) {
            throw new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_EXPIRED);
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

        refreshTokenRepository.save(username, newRefreshToken, jwtTokenProvider.getRefreshTokenTtl(false));

        return MemberJwtResult.of(
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

    private String extractToken(String bearerToken) {
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.AUTH_TOKEN_INVALID);
        }
        return bearerToken.substring(7).trim();
    }
}
