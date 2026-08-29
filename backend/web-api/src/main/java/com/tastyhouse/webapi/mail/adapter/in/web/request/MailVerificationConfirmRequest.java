package com.tastyhouse.webapi.mail.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import com.tastyhouse.webapi.mail.application.port.in.MailVerificationConfirmCommand;

@Schema(description = "메일 인증번호 확인 요청")
public record MailVerificationConfirmRequest(
    @NotBlank(message = "이메일을 입력해주세요.")
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$",
        message = "유효한 이메일 형식으로 입력해주세요."
    )
    @Schema(description = "이메일", example = "user@tastyhouse.com", requiredMode = Schema.RequiredMode.REQUIRED)
    String email,

    @NotBlank(message = "인증번호를 입력해주세요.")
    @Pattern(regexp = "^[0-9]{6}$", message = "인증번호는 6자리 숫자입니다.")
    @Schema(description = "메일로 발송된 6자리 인증번호", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    String verificationCode
) {

    public MailVerificationConfirmCommand toCommand() {
        return new MailVerificationConfirmCommand(email, verificationCode);
    }
}
