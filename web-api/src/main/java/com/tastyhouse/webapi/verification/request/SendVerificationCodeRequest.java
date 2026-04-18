package com.tastyhouse.webapi.verification.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SendVerificationCodeRequest(
    @NotBlank(message = "휴대폰번호를 입력해주세요.")
    @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰번호 형식이 아닙니다.")
    String phoneNumber
) {
}
