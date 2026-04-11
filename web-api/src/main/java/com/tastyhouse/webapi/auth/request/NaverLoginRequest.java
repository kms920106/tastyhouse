package com.tastyhouse.webapi.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "네이버 로그인 요청")
public record NaverLoginRequest(

    @Schema(description = "네이버 인증 서버로부터 받은 인가 코드", example = "abc123")
    @NotBlank(message = "인가 코드를 입력해주세요.")
    String code,

    @Schema(description = "CSRF 방지용 state 값 (인증 요청 시 생성한 랜덤 문자열)", example = "randomState123")
    @NotBlank(message = "state 값을 입력해주세요.")
    String state
) {}
