package com.tastyhouse.application.auth.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.application.auth.port.in.MemberKakaoLoginUseCase;
import com.tastyhouse.application.auth.port.out.SocialAuthorization;
import com.tastyhouse.application.auth.port.out.SocialLoginResult;
import com.tastyhouse.application.auth.port.out.SocialProvider;

@Service
class MemberKakaoLoginService implements MemberKakaoLoginUseCase {

    private final SocialLoginService socialLoginService;

    public MemberKakaoLoginService(SocialLoginService socialLoginService) {
        this.socialLoginService = socialLoginService;
    }

    @Override
    public SocialLoginResult kakaoLogin(String authorizationCode) {
        return socialLoginService.login(SocialProvider.KAKAO, SocialAuthorization.of(authorizationCode));
    }
}
