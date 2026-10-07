package com.tastyhouse.application.auth.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.application.auth.port.in.MemberFacebookLoginUseCase;
import com.tastyhouse.application.auth.port.out.SocialLoginResult;
import com.tastyhouse.application.auth.service.facebook.FacebookSocialLoginService;

@Service
class MemberFacebookLoginService implements MemberFacebookLoginUseCase {

    private final FacebookSocialLoginService facebookSocialLoginService;

    public MemberFacebookLoginService(FacebookSocialLoginService facebookSocialLoginService) {
        this.facebookSocialLoginService = facebookSocialLoginService;
    }

    @Override
    public SocialLoginResult facebookLogin(String accessToken) {
        return facebookSocialLoginService.login(accessToken);
    }
}
