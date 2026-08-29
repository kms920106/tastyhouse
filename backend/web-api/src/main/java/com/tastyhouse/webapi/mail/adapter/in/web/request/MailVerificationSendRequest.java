package com.tastyhouse.webapi.mail.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import com.tastyhouse.webapi.mail.application.port.in.MailVerificationSendCommand;

@Schema(description = "메일 인증번호 발송 요청")
public record MailVerificationSendRequest(
    @NotBlank(message = "이메일을 입력해주세요.")
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$",
        message = "유효한 이메일 형식으로 입력해주세요."
    )
    @Schema(description = "인증코드를 받을 이메일 주소", example = "user@tastyhouse.com", requiredMode = Schema.RequiredMode.REQUIRED)
    String email
) {

    public MailVerificationSendCommand toCommand() {
        return new MailVerificationSendCommand(email);
    }
}
