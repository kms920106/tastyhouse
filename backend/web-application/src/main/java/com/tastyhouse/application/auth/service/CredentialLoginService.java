package com.tastyhouse.application.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.model.MemberGender;
import com.tastyhouse.application.auth.port.out.MemberJwtResult;
import com.tastyhouse.application.auth.token.MemberTokenService;
import com.tastyhouse.application.member.service.MemberAuthService;
import com.tastyhouse.application.member.service.MemberRegistrationService;

@Service
public class CredentialLoginService {

    private final AuthenticationManager authenticationManager;
    private final MemberTokenService tokenService;
    private final MemberRegistrationService memberRegistrationService;
    private final PasswordEncoder passwordEncoder;
    private final MemberAuthService memberAuthService;

    public CredentialLoginService(
        AuthenticationManager authenticationManager,
        MemberTokenService tokenService,
        MemberRegistrationService memberRegistrationService,
        PasswordEncoder passwordEncoder,
        MemberAuthService memberAuthService
    ) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.memberRegistrationService = memberRegistrationService;
        this.passwordEncoder = passwordEncoder;
        this.memberAuthService = memberAuthService;
    }

    @Transactional
    public Long signUp(String username, String password,
                       String nickname, String fullName,
                       MemberGender gender, Integer birthDate, String phoneNumber,
                       boolean pushNotificationEnabled,
                       boolean marketingInfoEnabled, boolean eventInfoEnabled,
                       String smsVerifyToken, String mailVerifyToken,
                       String referrerNickname) {
        memberAuthService.verifySignUpTokens(phoneNumber, smsVerifyToken, username, mailVerifyToken);
        return memberRegistrationService.signUp(
            username, passwordEncoder.encode(password), nickname, fullName, gender, birthDate, phoneNumber,
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
