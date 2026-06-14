package com.tastyhouse.webapi.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "애플 로그인 요청")
public record AppleLoginRequest(

    @Schema(description = "Apple 인증 서버로부터 받은 인가 코드", example = "c1234abcd...")
    @NotBlank(message = "인가 코드를 입력해주세요.")
    String code
) {
}
