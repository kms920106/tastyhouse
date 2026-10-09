package com.tastyhouse.application.auth.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.application.auth.port.in.MemberNaverLoginUseCase;
import com.tastyhouse.application.auth.port.out.SocialAuthorization;
import com.tastyhouse.application.auth.port.out.SocialLoginResult;
import com.tastyhouse.application.auth.port.out.SocialProvider;

@Service
class MemberNaverLoginService implements MemberNaverLoginUseCase {

    private final SocialLoginService socialLoginService;

    public MemberNaverLoginService(SocialLoginService socialLoginService) {
        this.socialLoginService = socialLoginService;
    }

    @Override
    public SocialLoginResult naverLogin(String authorizationCode, String state) {
        return socialLoginService.login(SocialProvider.NAVER, SocialAuthorization.of(authorizationCode, state));
    }
}
