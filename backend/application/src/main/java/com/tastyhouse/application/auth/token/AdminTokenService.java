package com.tastyhouse.application.auth.token;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.admin.model.Admin;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.security.jwt.TokenType;
import com.tastyhouse.security.token.BlacklistRepository;
import com.tastyhouse.security.token.RefreshTokenRepository;
import com.tastyhouse.application.admin.service.AdminQueryService;
import com.tastyhouse.application.auth.port.out.AdminJwtResult;

@Service
@AdminApp
public class AdminTokenService {

    private final AdminJwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistRepository blacklistRepository;
    private final AdminQueryService adminQueryService;

    public AdminTokenService(
        AdminJwtTokenProvider jwtTokenProvider,
        RefreshTokenRepository refreshTokenRepository,
        BlacklistRepository blacklistRepository,
        AdminQueryService adminQueryService
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.blacklistRepository = blacklistRepository;
        this.adminQueryService = adminQueryService;
    }

    public AdminJwtResult issue(Authentication authentication, boolean rememberMe) {
        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication, rememberMe);

        refreshTokenRepository.save(
                authentication.getName(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenTtl(rememberMe)
        );

        return AdminJwtResult.of(accessToken, refreshToken, "Bearer");
    }

    public AdminJwtResult refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.ADMIN_AUTHENTICATION_FAILED, "유효하지 않은 Refresh Token입니다.");
        }
        jwtTokenProvider.validateTokenType(refreshToken, TokenType.REFRESH);

        String username = jwtTokenProvider.getUsernameFromJWT(refreshToken);

        if (refreshTokenRepository.isInvalid(username, refreshToken)) {
            throw new BusinessException(ErrorCode.ADMIN_AUTHENTICATION_FAILED, "만료되었거나 이미 로그아웃된 Refresh Token입니다.");
        }

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

        return AdminJwtResult.of(newAccessToken, newRefreshToken, "Bearer");
    }

    public void revoke(String bearerToken) {
        String accessToken = extractToken(bearerToken);

        if (jwtTokenProvider.validateToken(accessToken)) {

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
