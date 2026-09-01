package com.tastyhouse.webapplication.auth.port.out;

/**
 * 소셜 계정 연동 결과 — {@code status}로 분기하는 판별 유니온.
 *
 * <p><b>챕터 10</b>에서 신설. {@link SocialLoginResult}와 같은 이유로 서비스가 분기를 판정한다 —
 * smsVerifyToken의 전화번호로 가입된 회원이 있으면 연동 후 LOGIN, 없으면 NEEDS_SIGN_UP과 함께
 * 회원가입 폼 자동 매핑용 프로필을 싣는다.
 *
 * <p>{@code Status} 중첩과 거처의 근거는 {@link SocialLoginResult}와 같다.
 */
public record SocialLinkResult(
    Status status,
    String tempToken,
    JwtResult jwt,
    SocialProfileResult socialProfile
) {
    public enum Status {LOGIN, NEEDS_SIGN_UP}

    public static SocialLinkResult ofLogin(JwtResult jwt) {
        return new SocialLinkResult(
            Status.LOGIN,
            null,
            jwt,
            null
        );
    }

    public static SocialLinkResult ofSignUpRequired(String tempToken, SocialProfileResult socialProfile) {
        return new SocialLinkResult(
            Status.NEEDS_SIGN_UP,
            tempToken,
            null,
            socialProfile
        );
    }
}
