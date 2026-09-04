package com.tastyhouse.webapplication.auth.port.in;

import com.tastyhouse.webapplication.auth.port.out.MemberJwtResult;
import com.tastyhouse.webapplication.auth.port.out.PhoneLoginResult;
import com.tastyhouse.webapplication.auth.port.out.SocialLinkResult;
import com.tastyhouse.webapplication.auth.port.out.SocialLoginResult;

/**
 * 인증 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code AuthCommandService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p>인증은 로그인·토큰 발급/폐기·가입 등 상태를 바꾸는 연산만 있어 QueryUseCase 짝을 두지 않는다.
 * 서블릿/시큐리티 타입은 이 계약에 등장하지 않는다 — 컨트롤러가 원시값(Bearer 토큰 문자열·인가 코드 등)을
 * 추출해 넘기고, 시큐리티 컨텍스트 조작은 구현이 담당한다.
 *
 * <p><b>챕터 10</b> — 반환 타입이 표현 계약({@code Auth*Response})에서 프레임워크-프리 읽기 계약
 * ({@code webapplication.auth.port.out}의 {@code *Result})으로 바뀌었다. 인증은 Command 경로지만
 * 토큰·상태를 응답으로 되돌려주므로 조회 경로와 같은 규칙을 적용한다. 분기 판정(가입 필요 / 연동 필요 /
 * 로그인)은 여전히 구현이 하고, 표현 계약 조립만 web-api로 올라갔다.
 *
 * <p>{@code verifyPasswordResetCode}가 평문 {@code String}을 돌려주는 것은 대응 응답
 * ({@code AuthPasswordResetTokenResponse})이 토큰 하나만 감싸는 래퍼라 읽기 계약을 새로 만들 이유가
 * 없기 때문이다(mail·sms 인증 토큰과 같은 판단).
 */
public interface MemberAuthCommandUseCase {

    Long signUp(AuthSignUpCommand command);

    MemberJwtResult login(String username, String password, boolean rememberMe);

    MemberJwtResult refresh(String refreshToken);

    void logout(String bearerToken);

    void sendPasswordResetCode(String username);

    /**
     * @return 비밀번호 재설정 토큰(컨트롤러가 이 값으로 응답을 조립한다)
     */
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
