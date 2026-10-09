package com.tastyhouse.application.auth.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.application.auth.port.in.MemberAppleLoginUseCase;
import com.tastyhouse.application.auth.port.out.SocialAuthorization;
import com.tastyhouse.application.auth.port.out.SocialLoginResult;
import com.tastyhouse.application.auth.port.out.SocialProvider;

@Service
class MemberAppleLoginService implements MemberAppleLoginUseCase {

    private final SocialLoginService socialLoginService;

    public MemberAppleLoginService(SocialLoginService socialLoginService) {
        this.socialLoginService = socialLoginService;
    }

    @Override
    public SocialLoginResult appleLogin(String authorizationCode) {
        return socialLoginService.login(SocialProvider.APPLE, SocialAuthorization.of(authorizationCode));
    }
}
