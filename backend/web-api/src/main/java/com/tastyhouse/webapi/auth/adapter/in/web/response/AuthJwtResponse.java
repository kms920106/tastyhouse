package com.tastyhouse.webapi.auth.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.auth.port.out.MemberJwtResult;

@Schema(description = "JWT 인증 토큰 응답")
public record AuthJwtResponse(
    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.abc123")
    String accessToken,

    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.def456")
    String refreshToken,

    @Schema(description = "토큰 타입", example = "Bearer")
    String tokenType
) {

    public static AuthJwtResponse from(MemberJwtResult result) {
        return new AuthJwtResponse(
            result.accessToken(),
            result.refreshToken(),
            result.tokenType()
        );
    }
}
