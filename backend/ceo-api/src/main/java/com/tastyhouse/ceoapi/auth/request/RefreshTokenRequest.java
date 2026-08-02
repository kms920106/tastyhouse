package com.tastyhouse.ceoapi.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "토큰 갱신 요청")
public record RefreshTokenRequest(
    @NotBlank(message = "Refresh Token을 입력해주세요.")
    @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    String refreshToken
) {
}
