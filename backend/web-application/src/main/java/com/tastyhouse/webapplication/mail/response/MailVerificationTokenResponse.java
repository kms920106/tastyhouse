package com.tastyhouse.webapplication.mail.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메일 인증 토큰 응답")
public record MailVerificationTokenResponse(
    @Schema(description = "메일 인증 토큰", example = "b3c1e2f0-1234-4a5b-9c6d-7e8f9a0b1c2d")
    String mailVerifyToken
) {
    public static MailVerificationTokenResponse from(String mailVerifyToken) {
        return new MailVerificationTokenResponse(mailVerifyToken);
    }
}
