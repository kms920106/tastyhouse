package com.tastyhouse.webapi.auth.request;

import com.tastyhouse.core.domain.member.domain.model.SocialProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "소셜 계정 연동 요청. 소셜 로그인 응답에서 status=NEEDS_LINKING 또는 status=NEEDS_SIGN_UP을 받은 경우 호출합니다.")
public record SocialAccountLinkRequest(

    @Schema(description = "소셜 로그인 제공자 (KAKAO, NAVER, FACEBOOK, APPLE)", example = "KAKAO")
    @NotNull(message = "소셜 로그인 제공자를 입력해주세요.")
    SocialProvider provider,

    @Schema(description = "소셜 로그인 응답에서 발급된 임시 토큰 (10분 유효)", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank(message = "소셜 임시 토큰을 입력해주세요.")
    String tempToken,

    @Schema(description = "휴대폰 인증 완료 후 발급된 phoneVerifyToken (본인 확인용, 10분 유효)")
    @NotBlank(message = "휴대폰 인증 토큰을 입력해주세요.")
    String phoneVerifyToken
) {
}
