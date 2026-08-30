package com.tastyhouse.webapplication.auth.port.in;

import com.tastyhouse.webapplication.auth.response.AuthJwtResponse;
import com.tastyhouse.webapplication.auth.response.AuthPasswordResetTokenResponse;
import com.tastyhouse.webapplication.auth.response.AuthPhoneLoginResponse;
import com.tastyhouse.webapplication.auth.response.AuthSocialLinkResponse;
import com.tastyhouse.webapplication.auth.response.AuthSocialLoginResponse;

/**
 * 인증 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code AuthCommandService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p>인증은 로그인·토큰 발급/폐기·가입 등 상태를 바꾸는 연산만 있어 QueryUseCase 짝을 두지 않는다.
 * 서블릿/시큐리티 타입은 이 계약에 등장하지 않는다 — 컨트롤러가 원시값(Bearer 토큰 문자열·인가 코드 등)을
 * 추출해 넘기고, 시큐리티 컨텍스트 조작은 구현이 담당한다.
 */
public interface AuthCommandUseCase {

    Long signUp(AuthSignUpCommand command);

    AuthJwtResponse login(String username, String password, boolean rememberMe);

    AuthJwtResponse refresh(String refreshToken);

    void logout(String bearerToken);

    void sendPasswordResetCode(String username);

    AuthPasswordResetTokenResponse verifyPasswordResetCode(String username, String verificationCode);

    void resetPassword(String passwordResetToken, String newPassword, String newPasswordConfirm);

    AuthPhoneLoginResponse phoneLogin(String smsVerifyToken);

    AuthSocialLoginResponse kakaoLogin(String authorizationCode);

    AuthSocialLoginResponse naverLogin(String authorizationCode, String state);

    AuthSocialLoginResponse facebookLogin(String accessToken);

    AuthSocialLoginResponse appleLogin(String authorizationCode);

    AuthSocialLinkResponse linkAccount(String provider, String tempToken, String smsVerifyToken);

    AuthJwtResponse socialSignUp(AuthSocialSignUpCommand command);
}
