package com.tastyhouse.application.auth.port.out;

public record MemberJwtResult(
    String accessToken,
    String refreshToken,
    String tokenType
) {

    public static MemberJwtResult of(
        String accessToken,
        String refreshToken,
        String tokenType
    ) {
        return new MemberJwtResult(
            accessToken,
            refreshToken,
            tokenType
        );
    }
}
