package com.tastyhouse.webapi.sms.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import com.tastyhouse.webapi.sms.application.port.in.SmsVerificationSendCommand;

@Schema(description = "SMS 인증번호 발송 요청")
public record SmsVerificationSendRequest(
    @NotBlank(message = "휴대폰번호를 입력해주세요.")
    @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰번호 형식이 아닙니다.")
    @Schema(description = "휴대폰번호", example = "01012345678", requiredMode = Schema.RequiredMode.REQUIRED)
    String phoneNumber
) {

    public SmsVerificationSendCommand toCommand() {
        return new SmsVerificationSendCommand(phoneNumber);
    }
}
