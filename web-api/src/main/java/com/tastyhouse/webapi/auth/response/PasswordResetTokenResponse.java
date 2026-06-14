package com.tastyhouse.webapi.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 재설정 토큰 응답")
public record PasswordResetTokenResponse(
    @Schema(description = "비밀번호 재설정 토큰 (15분 유효)", example = "eyJhbGciOiJIUzI1NiJ9...")
    String passwordResetToken
) {
    public static PasswordResetTokenResponse from(String passwordResetToken) {
        return new PasswordResetTokenResponse(passwordResetToken);
    }
}
