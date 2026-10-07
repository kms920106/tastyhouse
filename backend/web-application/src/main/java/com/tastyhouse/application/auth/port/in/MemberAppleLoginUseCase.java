package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.application.auth.port.out.SocialLoginResult;

public interface MemberAppleLoginUseCase {

    SocialLoginResult appleLogin(String authorizationCode);
}
