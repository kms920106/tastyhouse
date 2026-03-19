package com.tastyhouse.webapi.auth;

import com.tastyhouse.webapi.auth.response.JwtResponse;
import com.tastyhouse.webapi.config.jwt.JwtProperties;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.config.jwt.TokenBlacklist;
import com.tastyhouse.webapi.config.jwt.TokenRedisRepository;
import com.tastyhouse.webapi.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final TokenBlacklist tokenBlacklist;
    private final TokenRedisRepository tokenRedisRepository;

    public JwtResponse login(String username, String password, boolean rememberMe) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication, rememberMe);

        tokenRedisRepository.saveRefreshToken(
            authentication.getName(),
            refreshToken,
            jwtTokenProvider.getRefreshTokenTtl(rememberMe)
        );

        return new JwtResponse(accessToken, refreshToken, "Bearer");
    }

    public void logout(String bearerToken) {
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            throw new UnauthorizedException("유효하지 않은 토큰입니다.");
        }
        String accessToken = bearerToken.substring(7).trim();
        if (jwtTokenProvider.validateToken(accessToken)) {
            tokenBlacklist.add(accessToken, jwtTokenProvider.getExpirationMillis(accessToken));
            tokenRedisRepository.deleteRefreshToken(jwtTokenProvider.getUsernameFromJWT(accessToken));
        }
        SecurityContextHolder.clearContext();
    }

    public JwtResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("유효하지 않은 Refresh Token입니다.");
        }

        String username = jwtTokenProvider.getUsernameFromJWT(refreshToken);

        if (!tokenRedisRepository.isRefreshTokenValid(username, refreshToken)) {
            throw new UnauthorizedException("만료되었거나 이미 로그아웃된 Refresh Token입니다.");
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

        tokenRedisRepository.saveRefreshToken(
            username,
            newRefreshToken,
            jwtProperties.getRefreshTokenExpiration()
        );

        return new JwtResponse(newAccessToken, newRefreshToken, "Bearer");
    }
}
