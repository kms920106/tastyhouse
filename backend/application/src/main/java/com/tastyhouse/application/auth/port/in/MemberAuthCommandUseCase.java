package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.application.auth.port.out.MemberJwtResult;
import com.tastyhouse.application.auth.port.out.PhoneLoginResult;
import com.tastyhouse.application.auth.port.out.SocialLinkResult;
import com.tastyhouse.application.auth.port.out.SocialLoginResult;

@WebApp
public interface MemberAuthCommandUseCase {

    Long signUp(AuthSignUpCommand command);

    MemberJwtResult login(String username, String password, boolean rememberMe);

    MemberJwtResult refresh(String refreshToken);

    void logout(String bearerToken);

    void sendPasswordResetCode(String username);

    String verifyPasswordResetCode(String username, String verificationCode);

    void resetPassword(String passwordResetToken, String newPassword, String newPasswordConfirm);

    PhoneLoginResult phoneLogin(String smsVerifyToken);

    SocialLoginResult kakaoLogin(String authorizationCode);

    SocialLoginResult naverLogin(String authorizationCode, String state);

    SocialLoginResult facebookLogin(String accessToken);

    SocialLoginResult appleLogin(String authorizationCode);

    SocialLinkResult linkAccount(String provider, String tempToken, String smsVerifyToken);

    MemberJwtResult socialSignUp(AuthSocialSignUpCommand command);
}
