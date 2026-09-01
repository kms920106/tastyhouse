package com.tastyhouse.webapi.auth.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 재설정 토큰 응답")
public record AuthPasswordResetTokenResponse(
    @Schema(description = "비밀번호 재설정 토큰 (15분 유효)", example = "eyJhbGciOiJIUzI1NiJ9...")
    String passwordResetToken
) {
    public static AuthPasswordResetTokenResponse from(String passwordResetToken) {
        return new AuthPasswordResetTokenResponse(passwordResetToken);
    }
}
