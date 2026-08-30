package com.tastyhouse.webapplication.sms.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "SMS 인증 토큰 응답")
public record SmsVerificationTokenResponse(
    @Schema(description = "SMS 인증 토큰", example = "a1b2c3d4-5678-4e9f-a0b1-c2d3e4f5a6b7")
    String smsVerifyToken
) {
    public static SmsVerificationTokenResponse from(String smsVerifyToken) {
        return new SmsVerificationTokenResponse(smsVerifyToken);
    }
}
