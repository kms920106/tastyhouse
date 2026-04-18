package com.tastyhouse.webapi.verification.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ConfirmEmailVerificationCodeRequest(
    @NotBlank(message = "이메일을 입력해주세요.")
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$",
        message = "유효한 이메일 형식으로 입력해주세요."
    )
    String email,

    @NotBlank(message = "인증번호를 입력해주세요.")
    @Pattern(regexp = "^[0-9]{6}$", message = "인증번호는 6자리 숫자입니다.")
    String verificationCode
) {
}
