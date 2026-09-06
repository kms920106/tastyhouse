package com.tastyhouse.application.auth.port.out;

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
