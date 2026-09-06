package com.tastyhouse.application.auth.token;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.security.token.BlacklistRepository;
import com.tastyhouse.security.token.RefreshTokenRepository;
import com.tastyhouse.application.auth.security.MemberUserDetails;
import com.tastyhouse.application.auth.port.out.MemberJwtResult;

@Service
@WebApp
public class MemberTokenService {

    private final MemberJwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistRepository blacklistRepository;

    public MemberTokenService(
        MemberJwtTokenProvider jwtTokenProvider,
        RefreshTokenRepository refreshTokenRepository,
        BlacklistRepository blacklistRepository
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.blacklistRepository = blacklistRepository;
    }

    public MemberJwtResult issue(Member member, boolean rememberMe) {
        MemberUserDetails userDetails = new MemberUserDetails(
            member.getId(),
            member.getUsername(),
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        return issue(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()), rememberMe);
    }

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

    public void revoke(String bearerToken) {
        String accessToken = extractToken(bearerToken);

        if (jwtTokenProvider.validateToken(accessToken)) {
            blacklistRepository.add(accessToken, jwtTokenProvider.getExpirationMillis(accessToken));
            refreshTokenRepository.delete(jwtTokenProvider.getUsernameFromJWT(accessToken));
        }
    }

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
