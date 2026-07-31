package com.tastyhouse.webapi.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.domain.model.MemberGender;
import com.tastyhouse.webapi.config.jwt.service.TokenService;
import com.tastyhouse.webapi.auth.response.AuthJwtResponse;
import com.tastyhouse.webapi.member.service.MemberCommandService;
import com.tastyhouse.webapi.member.service.MemberAuthService;

@Service
@RequiredArgsConstructor
public class CredentialLoginService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final MemberCommandService memberCommandService;
    private final MemberAuthService memberAuthService;

    // 회원가입 토큰 검증 후 신규 회원을 등록
    public void signUp(String username, String password,
                       String nickname, String fullName,
                       MemberGender gender, Integer birthDate, String phoneNumber,
                       boolean pushNotificationEnabled,
                       boolean marketingInfoEnabled, boolean eventInfoEnabled,
                       String smsVerifyToken, String mailVerifyToken,
                       String referrerNickname) {
        memberAuthService.verifySignUpTokens(phoneNumber, smsVerifyToken, username, mailVerifyToken);
        memberCommandService.signUp(
            username, password, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled,
            referrerNickname
        );
    }

    // 아이디/비밀번호 인증 후 JWT 토큰을 발급
    public AuthJwtResponse login(String username, String password, boolean rememberMe) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return tokenService.issue(authentication, rememberMe);
    }

    // 리프레시 토큰으로 새 JWT 토큰을 재발급
    public AuthJwtResponse refresh(String refreshToken) {
        return tokenService.refresh(refreshToken);
    }

    // 토큰을 무효화하고 보안 컨텍스트를 초기화하여 로그아웃 처리
    public void logout(String bearerToken) {
        tokenService.revoke(bearerToken);
        SecurityContextHolder.clearContext();
    }
}
