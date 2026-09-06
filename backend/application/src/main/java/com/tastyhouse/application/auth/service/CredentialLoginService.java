package com.tastyhouse.application.auth.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.model.MemberGender;
import com.tastyhouse.application.auth.token.MemberTokenService;
import com.tastyhouse.application.member.service.MemberCommandService;
import com.tastyhouse.application.member.service.MemberAuthService;
import com.tastyhouse.application.auth.port.out.MemberJwtResult;

@Service
@WebApp
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

    public MemberJwtResult login(String username, String password, boolean rememberMe) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return tokenService.issue(authentication, rememberMe);
    }

    public MemberJwtResult refresh(String refreshToken) {
        return tokenService.refresh(refreshToken);
    }

    public void logout(String bearerToken) {
        tokenService.revoke(bearerToken);
        SecurityContextHolder.clearContext();
    }
}
