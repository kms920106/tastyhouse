package com.tastyhouse.application.auth.port.out;

public record SocialLinkResult(
    Status status,
    String tempToken,
    MemberJwtResult jwt,
    SocialProfileResult socialProfile
) {
    public enum Status {LOGIN, NEEDS_SIGN_UP}

    public static SocialLinkResult ofLogin(MemberJwtResult jwt) {
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
