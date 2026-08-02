package com.tastyhouse.webapi.sms.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "SMS 인증번호 확인 요청")
public record SmsVerificationConfirmRequest(
    @NotBlank(message = "휴대폰번호를 입력해주세요.")
    @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰번호 형식이 아닙니다.")
    @Schema(description = "휴대폰번호", example = "01012345678", requiredMode = Schema.RequiredMode.REQUIRED)
    String phoneNumber,

    @NotBlank(message = "인증번호를 입력해주세요.")
    @Pattern(regexp = "^[0-9]{6}$", message = "인증번호는 6자리 숫자입니다.")
    @Schema(description = "인증번호", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    String verificationCode
) {
}
