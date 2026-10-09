package com.tastyhouse.application.auth.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.application.auth.port.in.MemberFacebookLoginUseCase;
import com.tastyhouse.application.auth.port.out.SocialAuthorization;
import com.tastyhouse.application.auth.port.out.SocialLoginResult;
import com.tastyhouse.application.auth.port.out.SocialProvider;

@Service
class MemberFacebookLoginService implements MemberFacebookLoginUseCase {

    private final SocialLoginService socialLoginService;

    public MemberFacebookLoginService(SocialLoginService socialLoginService) {
        this.socialLoginService = socialLoginService;
    }

    @Override
    public SocialLoginResult facebookLogin(String accessToken) {
        return socialLoginService.login(SocialProvider.FACEBOOK, SocialAuthorization.of(accessToken));
    }
}
