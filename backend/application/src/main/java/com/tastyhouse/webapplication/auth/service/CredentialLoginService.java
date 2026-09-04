package com.tastyhouse.webapplication.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.model.MemberGender;
import com.tastyhouse.webapplication.auth.token.MemberTokenService;
import com.tastyhouse.webapplication.member.service.MemberCommandService;
import com.tastyhouse.webapplication.member.service.MemberAuthService;
import com.tastyhouse.webapplication.auth.port.out.MemberJwtResult;

@Service
public class CredentialLoginService {

    private final AuthenticationManager authenticationManager;
    private final MemberTokenService tokenService;
    private final MemberCommandService memberCommandService;
    private final MemberAuthService memberAuthService;

    public CredentialLoginService(
        AuthenticationManager authenticationManager,
        MemberTokenService tokenService,
        MemberCommandService memberCommandService,
        MemberAuthService memberAuthService
    ) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.memberCommandService = memberCommandService;
        this.memberAuthService = memberAuthService;
    }

    // 회원가입 토큰 검증 후 신규 회원을 등록하고 생성된 회원 식별자를 반환
    public Long signUp(String username, String password,
                       String nickname, String fullName,
                       MemberGender gender, Integer birthDate, String phoneNumber,
                       boolean pushNotificationEnabled,
                       boolean marketingInfoEnabled, boolean eventInfoEnabled,
                       String smsVerifyToken, String mailVerifyToken,
                       String referrerNickname) {
        memberAuthService.verifySignUpTokens(phoneNumber, smsVerifyToken, username, mailVerifyToken);
        return memberCommandService.signUp(
            username, password, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled,
            referrerNickname
        );
    }

    // 아이디/비밀번호 인증 후 JWT 토큰을 발급
    public MemberJwtResult login(String username, String password, boolean rememberMe) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return tokenService.issue(authentication, rememberMe);
    }

    // 리프레시 토큰으로 새 JWT 토큰을 재발급
    public MemberJwtResult refresh(String refreshToken) {
        return tokenService.refresh(refreshToken);
    }

    // 토큰을 무효화하고 보안 컨텍스트를 초기화하여 로그아웃 처리
    public void logout(String bearerToken) {
        tokenService.revoke(bearerToken);
        SecurityContextHolder.clearContext();
    }
}
