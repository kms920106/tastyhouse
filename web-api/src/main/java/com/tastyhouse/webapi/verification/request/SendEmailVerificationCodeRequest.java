package com.tastyhouse.webapi.verification.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SendEmailVerificationCodeRequest(
        @NotBlank(message = "이메일을 입력해주세요.")
        @Pattern(
            regexp = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$",
            message = "유효한 이메일 형식으로 입력해주세요."
        )
        String email
) {
}
