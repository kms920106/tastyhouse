package com.tastyhouse.webapi.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "애플 계정 연동 요청. /api/auth/v1/login/apple에서 status=NEEDS_LINKING 또는 status=NEEDS_SIGN_UP을 받은 경우 호출합니다.")
public record AppleAccountLinkRequest(

    @Schema(description = "애플 로그인 응답에서 발급된 임시 토큰 (10분 유효)", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank(message = "애플 임시 토큰을 입력해주세요.")
    String appleTempToken,

    @Schema(description = "휴대폰 인증 완료 후 발급된 phoneVerifyToken (본인 확인용, 10분 유효)")
    @NotBlank(message = "휴대폰 인증 토큰을 입력해주세요.")
    String phoneVerifyToken
) {}
