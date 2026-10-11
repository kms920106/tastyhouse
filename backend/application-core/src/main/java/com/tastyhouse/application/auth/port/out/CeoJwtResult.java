package com.tastyhouse.application.auth.port.out;

public record CeoJwtResult(
    String accessToken,
    String refreshToken,
    String tokenType
) {

    public static CeoJwtResult of(
        String accessToken,
        String refreshToken,
        String tokenType
    ) {
        return new CeoJwtResult(
            accessToken,
            refreshToken,
            tokenType
        );
    }
}
