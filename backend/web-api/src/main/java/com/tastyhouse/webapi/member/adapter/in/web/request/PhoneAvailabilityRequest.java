package com.tastyhouse.webapi.member.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "휴대폰번호 가입 가능 여부 확인 요청")
public record PhoneAvailabilityRequest(
    @NotBlank(message = "휴대폰번호를 입력해주세요.")
    @Schema(description = "확인할 휴대폰번호", example = "01099841511", requiredMode = Schema.RequiredMode.REQUIRED)
    String phoneNumber
) {
}
