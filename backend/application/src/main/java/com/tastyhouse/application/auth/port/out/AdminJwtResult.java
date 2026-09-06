package com.tastyhouse.application.auth.port.out;

public record AdminJwtResult(
    String accessToken,
    String refreshToken,
    String tokenType
) {

    public static AdminJwtResult of(
        String accessToken,
        String refreshToken,
        String tokenType
    ) {
        return new AdminJwtResult(
            accessToken,
            refreshToken,
            tokenType
        );
    }
}
