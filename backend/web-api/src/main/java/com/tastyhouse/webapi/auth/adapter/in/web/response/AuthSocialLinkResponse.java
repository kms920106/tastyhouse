package com.tastyhouse.webapi.auth.adapter.in.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.auth.port.out.SocialLinkResult;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "소셜 계정 연동 응답. status 필드로 분기: LOGIN / NEEDS_SIGN_UP")
public record AuthSocialLinkResponse(

    @Schema(description = "처리 결과 상태. LOGIN=연동 및 로그인 성공, NEEDS_SIGN_UP=해당 전화번호로 가입된 계정 없어 신규 회원가입 필요")
    Status status,

    @Schema(description = "소셜 임시 토큰 (10분 유효). NEEDS_SIGN_UP 시 반환. 회원가입 API 호출에 사용하며 1회 사용 후 폐기됩니다.")
    String tempToken,

    @Schema(description = "앱 JWT 토큰 정보. status=LOGIN일 때만 반환")
    AuthJwtResponse jwt,

    @Schema(description = "소셜 프로필 정보. status=NEEDS_SIGN_UP일 때 반환")
    AuthSocialProfileResponse socialProfile
) {
    public enum Status {LOGIN, NEEDS_SIGN_UP}

    /**
     * 분기 판정은 서비스가 끝냈다 — 여기서는 읽기 계약을 표현 계약으로 옮겨 담기만 한다.
     * {@code jwt}·{@code socialProfile}은 status에 따라 한쪽만 채워지므로 null을 그대로 통과시킨다
     * ({@code @JsonInclude(NON_NULL)}이 직렬화에서 생략하는 것이 계약이다).
     */
    public static AuthSocialLinkResponse from(SocialLinkResult result) {
        return new AuthSocialLinkResponse(
            Status.valueOf(result.status().name()),
            result.tempToken(),
            result.jwt() == null ? null : AuthJwtResponse.from(result.jwt()),
            result.socialProfile() == null ? null : AuthSocialProfileResponse.from(result.socialProfile())
        );
    }
}
