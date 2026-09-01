package com.tastyhouse.adminapi.auth.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.adminapplication.auth.port.out.JwtResult;

@Schema(description = "JWT 토큰 응답")
public record JwtResponse(
    @Schema(description = "Access Token") String accessToken,
    @Schema(description = "Refresh Token") String refreshToken,
    @Schema(description = "토큰 타입", example = "Bearer") String tokenType
) {
    public static JwtResponse from(JwtResult result) {
        return new JwtResponse(
            result.accessToken(),
            result.refreshToken(),
            result.tokenType()
        );
    }
}
