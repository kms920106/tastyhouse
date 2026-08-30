package com.tastyhouse.ceoapplication.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT 토큰 응답")
public record JwtResponse(
    @Schema(description = "Access Token") String accessToken,
    @Schema(description = "Refresh Token") String refreshToken,
    @Schema(description = "토큰 타입", example = "Bearer") String tokenType
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
