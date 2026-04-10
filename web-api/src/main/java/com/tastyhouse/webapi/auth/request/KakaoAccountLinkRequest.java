package com.tastyhouse.webapi.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "카카오 계정 연동 요청. /api/auth/v1/login/kakao에서 status=NEEDS_LINKING을 받은 경우 호출합니다.")
public record KakaoAccountLinkRequest(

    @Schema(description = "카카오 사용자 정보 조회에 사용할 액세스 토큰", example = "ya29.abc123...")
    @NotBlank(message = "카카오 액세스 토큰을 입력해주세요.")
    String accessToken,

    @Schema(description = "휴대폰 인증 완료 후 발급된 phoneVerifyToken (본인 확인용, 10분 유효)")
    @NotBlank(message = "휴대폰 인증 토큰을 입력해주세요.")
    String phoneVerifyToken
) {
}
