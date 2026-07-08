package com.tastyhouse.webapi.verification.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 인증 토큰 응답")
public record EmailVerifyTokenResponse(
    @Schema(description = "이메일 인증 토큰", example = "b3c1e2f0-1234-4a5b-9c6d-7e8f9a0b1c2d")
    String emailVerifyToken
) {
    public static EmailVerifyTokenResponse from(String emailVerifyToken) {
        return new EmailVerifyTokenResponse(emailVerifyToken);
    }
}
