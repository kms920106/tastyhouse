package com.tastyhouse.application.auth.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.application.auth.port.in.MemberKakaoLoginUseCase;
import com.tastyhouse.application.auth.port.out.SocialLoginResult;
import com.tastyhouse.application.auth.service.kakao.KakaoSocialLoginService;

@Service
class MemberKakaoLoginService implements MemberKakaoLoginUseCase {

    private final KakaoSocialLoginService kakaoSocialLoginService;

    public MemberKakaoLoginService(KakaoSocialLoginService kakaoSocialLoginService) {
        this.kakaoSocialLoginService = kakaoSocialLoginService;
    }

    @Override
    public SocialLoginResult kakaoLogin(String authorizationCode) {
        return kakaoSocialLoginService.login(authorizationCode);
    }
}
