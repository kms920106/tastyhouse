package com.tastyhouse.webapi.verification.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "휴대폰 인증 토큰 응답")
public record VerificationPhoneTokenResponse(
    @Schema(description = "휴대폰 인증 토큰", example = "a1b2c3d4-5678-4e9f-a0b1-c2d3e4f5a6b7")
    String phoneVerifyToken
) {
    public static VerificationPhoneTokenResponse from(String phoneVerifyToken) {
        return new VerificationPhoneTokenResponse(phoneVerifyToken);
    }
}
