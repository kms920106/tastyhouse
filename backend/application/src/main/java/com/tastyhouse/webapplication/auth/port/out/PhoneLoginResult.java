package com.tastyhouse.webapplication.auth.port.out;

/**
 * 휴대폰 인증 로그인 결과 — 가입 필요 여부와 JWT.
 *
 * <p><b>챕터 10</b>에서 신설. 가입 여부 판정은 서비스가 하고, web-api는 결과를 옮겨 담기만 한다.
 * 거처의 근거는 {@link MemberJwtResult}와 같다.
 */
public record PhoneLoginResult(
    boolean needsSignUp,
    MemberJwtResult jwt
) {

    public static PhoneLoginResult ofLogin(MemberJwtResult jwt) {
        return new PhoneLoginResult(
            false,
            jwt
        );
    }

    public static PhoneLoginResult ofSignUpRequired() {
        return new PhoneLoginResult(
            true,
            null
        );
    }
}
