package com.tastyhouse.webapi.member.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 인증 응답")
public record VerifyPasswordResponse(
    @Schema(description = "개인정보 수정용 단기 인증 토큰 (5분 유효)", example = "eyJhbGciOiJIUzI1NiJ9...")
    String verifyToken
) {
    public static VerifyPasswordResponse from(String verifyToken) {
        return new VerifyPasswordResponse(verifyToken);
    }
}
