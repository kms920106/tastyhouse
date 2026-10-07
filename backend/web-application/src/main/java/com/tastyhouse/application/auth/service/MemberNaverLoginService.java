package com.tastyhouse.application.auth.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.application.auth.port.in.MemberNaverLoginUseCase;
import com.tastyhouse.application.auth.port.out.SocialLoginResult;
import com.tastyhouse.application.auth.service.naver.NaverSocialLoginService;

@Service
class MemberNaverLoginService implements MemberNaverLoginUseCase {

    private final NaverSocialLoginService naverSocialLoginService;

    public MemberNaverLoginService(NaverSocialLoginService naverSocialLoginService) {
        this.naverSocialLoginService = naverSocialLoginService;
    }

    @Override
    public SocialLoginResult naverLogin(String authorizationCode, String state) {
        return naverSocialLoginService.login(authorizationCode, state);
    }
}
