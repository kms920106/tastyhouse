package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.application.auth.port.out.SocialLoginResult;

public interface MemberKakaoLoginUseCase {

    SocialLoginResult kakaoLogin(String authorizationCode);
}
