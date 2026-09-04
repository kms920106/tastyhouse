package com.tastyhouse.application.auth.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.tastyhouse.application.auth.port.in.AdminAuthCommandUseCase;
import com.tastyhouse.application.auth.port.in.AdminAuthLoginCommand;
import com.tastyhouse.application.auth.port.out.AdminJwtResult;
import com.tastyhouse.application.auth.token.AdminTokenService;

/**
 * 관리자 인증(로그인/토큰갱신/로그아웃) 서비스.
 * 자격증명 검증은 Spring Security AuthenticationManager(+AdminUserDetailsService)에 위임한다.
 *
 * <p>이 서비스와 {@link AdminTokenService}는 web-api의 동명 타입과 <b>의도적으로 중복</b>된다 —
 * 인증 주체가 {@code Admin}이고 {@code ADMIN_*} ErrorCode와 {@code JWT_SECRET_ADMIN}을 쓰므로
 * 통합하면 앱별 인증 경계가 무너진다(backend/CLAUDE.md 앱별 중복 허용 목록).
 */
@Service
@AdminApp
public class AdminAuthCommandService implements AdminAuthCommandUseCase {

    private final AuthenticationManager authenticationManager;
    private final AdminTokenService tokenService;

    public AdminAuthCommandService(AuthenticationManager authenticationManager, AdminTokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    /**
     * 아이디/비밀번호 인증 후 JWT 토큰을 발급한다.
     */
    @Override
    public AdminJwtResult login(AdminAuthLoginCommand command) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(command.username(), command.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return tokenService.issue(authentication, command.rememberMe());
    }

    /**
     * 리프레시 토큰으로 새 JWT 토큰을 재발급한다.
     */
    @Override
    public AdminJwtResult refresh(String refreshToken) {
        return tokenService.refresh(refreshToken);
    }

    /**
     * 토큰을 무효화하고 보안 컨텍스트를 초기화하여 로그아웃 처리한다.
     */
    @Override
    public void logout(String bearerToken) {
        tokenService.revoke(bearerToken);
        SecurityContextHolder.clearContext();
    }
}
