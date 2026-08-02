package com.tastyhouse.webapi.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "비밀번호 찾기 인증코드 확인 요청")
public record PasswordResetVerifyRequest(

    @Schema(description = "아이디 (이메일 형식)", example = "user@example.com")
    @NotBlank(message = "아이디를 입력해주세요.")
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$",
        message = "유효한 이메일 형식으로 입력해주세요."
    )
    String username,

    @Schema(description = "이메일로 발송된 6자리 인증코드", example = "482931")
    @NotBlank(message = "인증코드를 입력해주세요.")
    String verificationCode
) {
}
