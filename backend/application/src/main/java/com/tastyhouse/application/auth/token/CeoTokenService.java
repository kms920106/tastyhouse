package com.tastyhouse.application.auth.token;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.tastyhouse.application.auth.port.out.CeoJwtResult;
import com.tastyhouse.application.ceo.service.CeoOwnerQueryService;
import com.tastyhouse.domain.ceo.model.Ceo;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.security.jwt.TokenType;
import com.tastyhouse.security.token.BlacklistRepository;
import com.tastyhouse.security.token.RefreshTokenRepository;

@Service
@CeoApp
public class CeoTokenService {

    private final CeoJwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistRepository blacklistRepository;
    private final CeoOwnerQueryService ceoQueryService;

    public CeoTokenService(
        CeoJwtTokenProvider jwtTokenProvider,
        RefreshTokenRepository refreshTokenRepository,
        BlacklistRepository blacklistRepository,
        CeoOwnerQueryService ceoQueryService
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.blacklistRepository = blacklistRepository;
        this.ceoQueryService = ceoQueryService;
    }

    public CeoJwtResult issue(Authentication authentication, boolean rememberMe) {
        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication, rememberMe);

        refreshTokenRepository.save(
                authentication.getName(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenTtl(rememberMe)
        );

        return CeoJwtResult.of(accessToken, refreshToken, "Bearer");
    }

    public CeoJwtResult refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.CEO_AUTHENTICATION_FAILED, "유효하지 않은 Refresh Token입니다.");
        }
        jwtTokenProvider.validateTokenType(refreshToken, TokenType.REFRESH);

        String username = jwtTokenProvider.getUsernameFromJWT(refreshToken);

        if (refreshTokenRepository.isInvalid(username, refreshToken)) {
            throw new BusinessException(ErrorCode.CEO_AUTHENTICATION_FAILED, "만료되었거나 이미 로그아웃된 Refresh Token입니다.");
        }

        Ceo ceo = ceoQueryService.findByUsername(username)
            .orElseThrow(() -> new BusinessException(ErrorCode.CEO_AUTHENTICATION_FAILED, "존재하지 않는 점주입니다."));
        if (!ceo.isActive()) {
            refreshTokenRepository.delete(username);
            throw new BusinessException(ErrorCode.CEO_ACCOUNT_INACTIVE);
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

        refreshTokenRepository.save(username, newRefreshToken, jwtTokenProvider.getRefreshTokenTtl(false));

        return CeoJwtResult.of(newAccessToken, newRefreshToken, "Bearer");
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
            throw new BusinessException(ErrorCode.CEO_AUTHENTICATION_FAILED, "유효하지 않은 토큰입니다.");
        }
        return bearerToken.substring(7).trim();
    }
}
