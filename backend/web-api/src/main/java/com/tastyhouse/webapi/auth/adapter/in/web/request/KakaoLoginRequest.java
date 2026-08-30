package com.tastyhouse.webapi.auth.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "카카오 로그인 요청")
public record KakaoLoginRequest(

    @Schema(description = "카카오 인증 서버로부터 받은 인가 코드", example = "abc123")
    @NotBlank(message = "인가 코드를 입력해주세요.")
    String code
) {}
