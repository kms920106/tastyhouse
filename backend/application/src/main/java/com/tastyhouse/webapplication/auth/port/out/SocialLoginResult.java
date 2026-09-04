package com.tastyhouse.webapplication.auth.port.out;

/**
 * 소셜 로그인 결과 — {@code status}로 분기하는 판별 유니온.
 *
 * <p><b>챕터 10</b>에서 신설. 카카오·네이버·페이스북·애플 4개 소셜 로그인 서비스가 공유하며, 어느
 * 분기로 갈지는 <b>서비스가 판정</b>한다(기존 소셜 계정 존재 / 동일 이메일 일반가입 계정 존재 / 신규).
 * web-api는 판정하지 않고 이 결과를 표현 계약으로 옮겨 담기만 한다.
 *
 * <p>{@code Status}를 이 record 안에 중첩해 둔 것은 승격 전
 * {@code AuthSocialLoginResponse.Status}의 구조를 그대로 승계한 것이다 — 상수 이름이 그대로 JSON
 * 값이 되므로 이름을 바꾸면 API가 바뀐다.
 *
 * <p>거처가 앱 네임스페이스인 근거는 {@link JwtResult}와 같다(인증은 앱별 계약).
 */
public record SocialLoginResult(
    Status status,
    String tempToken,
    JwtResult jwt
) {
    public enum Status {LOGIN, NEEDS_SIGN_UP, NEEDS_LINKING}

    public static SocialLoginResult ofLogin(JwtResult jwt) {
        return new SocialLoginResult(
            Status.LOGIN,
            null,
            jwt
        );
    }

    public static SocialLoginResult ofSignUpRequired(String tempToken) {
        return new SocialLoginResult(
            Status.NEEDS_SIGN_UP,
            tempToken,
            null
        );
    }

    public static SocialLoginResult ofLinkingRequired(String tempToken) {
        return new SocialLoginResult(
            Status.NEEDS_LINKING,
            tempToken,
            null
        );
    }
}
