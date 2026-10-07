package com.tastyhouse.application.auth.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.application.auth.port.in.MemberAppleLoginUseCase;
import com.tastyhouse.application.auth.port.out.SocialLoginResult;
import com.tastyhouse.application.auth.service.apple.AppleSocialLoginService;

@Service
class MemberAppleLoginService implements MemberAppleLoginUseCase {

    private final AppleSocialLoginService appleSocialLoginService;

    public MemberAppleLoginService(AppleSocialLoginService appleSocialLoginService) {
        this.appleSocialLoginService = appleSocialLoginService;
    }

    @Override
    public SocialLoginResult appleLogin(String authorizationCode) {
        return appleSocialLoginService.login(authorizationCode);
    }
}
