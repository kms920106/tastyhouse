package com.tastyhouse.webapi.auth.service;

import com.tastyhouse.core.entity.user.Gender;
import com.tastyhouse.webapi.auth.response.JwtResponse;
import com.tastyhouse.webapi.config.jwt.service.TokenService;
import com.tastyhouse.webapi.member.service.MemberAccountService;
import com.tastyhouse.webapi.member.service.MemberAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final MemberAccountService memberAccountService;
    private final MemberAuthService memberAuthService;

    // 회원가입 토큰 검증 후 신규 회원을 등록
    public void signUp(String username, String password,
                       String nickname, String fullName,
                       Gender gender, Integer birthDate, String phoneNumber,
                       Boolean pushNotificationEnabled,
                       Boolean marketingInfoEnabled, Boolean eventInfoEnabled,
                       String phoneVerifyToken, String emailVerifyToken,
                       String referrerNickname) {
        memberAuthService.verifySignUpTokens(phoneNumber, phoneVerifyToken, username, emailVerifyToken);
        memberAccountService.signUp(
            username, password, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled,
            referrerNickname
        );
    }

    // 아이디/비밀번호 인증 후 JWT 토큰을 발급
    public JwtResponse login(String username, String password, boolean rememberMe) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return tokenService.issue(authentication, rememberMe);
    }

    // 리프레시 토큰으로 새 JWT 토큰을 재발급
    public JwtResponse refresh(String refreshToken) {
        return tokenService.refresh(refreshToken);
    }

    // 토큰을 무효화하고 보안 컨텍스트를 초기화하여 로그아웃 처리
    public void logout(String bearerToken) {
        tokenService.revoke(bearerToken);
        SecurityContextHolder.clearContext();
    }
}
