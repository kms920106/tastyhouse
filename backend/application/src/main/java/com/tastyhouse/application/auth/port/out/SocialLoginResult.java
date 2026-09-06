package com.tastyhouse.application.auth.port.out;

public record SocialLoginResult(
    Status status,
    String tempToken,
    MemberJwtResult jwt
) {
    public enum Status {LOGIN, NEEDS_SIGN_UP, NEEDS_LINKING}

    public static SocialLoginResult ofLogin(MemberJwtResult jwt) {
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
