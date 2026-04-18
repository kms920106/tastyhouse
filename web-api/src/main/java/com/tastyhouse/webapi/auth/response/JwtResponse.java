package com.tastyhouse.webapi.auth.response;

public record JwtResponse(
    String accessToken,
    String refreshToken,
    String tokenType
) {
    public static JwtResponse of(
    String accessToken,
    String refreshToken,
    String tokenType
    ) {
    return new JwtResponse(
        accessToken,
        refreshToken,
        tokenType
    );
    }
}
