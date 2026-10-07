package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.application.auth.port.out.SocialLoginResult;

public interface MemberNaverLoginUseCase {

    SocialLoginResult naverLogin(String authorizationCode, String state);
}
