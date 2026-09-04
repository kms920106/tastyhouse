package com.tastyhouse.ceoapi.auth.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.auth.port.out.CeoJwtResult;

@Schema(description = "JWT 토큰 응답")
public record JwtResponse(
    @Schema(description = "Access Token") String accessToken,
    @Schema(description = "Refresh Token") String refreshToken,
    @Schema(description = "토큰 타입", example = "Bearer") String tokenType
) {
    public static JwtResponse from(CeoJwtResult result) {
        return new JwtResponse(
            result.accessToken(),
            result.refreshToken(),
            result.tokenType()
        );
    }
}
