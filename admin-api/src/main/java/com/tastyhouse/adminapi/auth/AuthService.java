package com.tastyhouse.adminapi.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.tastyhouse.adminapi.config.jwt.service.TokenService;
import com.tastyhouse.adminapi.auth.response.JwtResponse;

/**
 * 관리자 인증(로그인/토큰갱신/로그아웃) 서비스.
 * 자격증명 검증은 Spring Security AuthenticationManager(+AdminUserDetailsService)에 위임한다.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthService(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    /**
     * 아이디/비밀번호 인증 후 JWT 토큰을 발급한다.
     */
    public JwtResponse login(String username, String password, boolean rememberMe) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return tokenService.issue(authentication, rememberMe);
    }

    /**
     * 리프레시 토큰으로 새 JWT 토큰을 재발급한다.
     */
    public JwtResponse refresh(String refreshToken) {
        return tokenService.refresh(refreshToken);
    }

    /**
     * 토큰을 무효화하고 보안 컨텍스트를 초기화하여 로그아웃 처리한다.
     */
    public void logout(String bearerToken) {
        tokenService.revoke(bearerToken);
        SecurityContextHolder.clearContext();
    }
}
