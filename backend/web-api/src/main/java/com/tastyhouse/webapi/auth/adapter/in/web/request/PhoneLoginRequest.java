package com.tastyhouse.webapi.auth.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "휴대폰 인증 로그인 요청")
public record PhoneLoginRequest(

    @Schema(description = "휴대폰 인증 완료 후 발급된 smsVerifyToken (10분 유효)")
    @NotBlank(message = "휴대폰 인증 토큰은 필수입니다.")
    String smsVerifyToken
) {
}
