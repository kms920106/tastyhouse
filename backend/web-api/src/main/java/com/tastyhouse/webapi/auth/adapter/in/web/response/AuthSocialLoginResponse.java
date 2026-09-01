package com.tastyhouse.webapi.auth.adapter.in.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.webapplication.auth.port.out.SocialLoginResult;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "소셜 로그인 응답. status 필드로 분기: LOGIN / NEEDS_SIGN_UP / NEEDS_LINKING")
public record AuthSocialLoginResponse(

    @Schema(description = "처리 결과 상태. LOGIN=로그인 성공, NEEDS_SIGN_UP=신규 회원가입 필요, NEEDS_LINKING=기존 일반가입 계정과 소셜 연동 필요")
    Status status,

    @Schema(description = "소셜 임시 토큰 (10분 유효). NEEDS_SIGN_UP/NEEDS_LINKING 시 반환. 회원가입·연동 API 호출에 사용하며 1회 사용 후 폐기됩니다.")
    String tempToken,

    @Schema(description = "앱 JWT 토큰 정보. status=LOGIN일 때만 반환")
    AuthJwtResponse jwt
) {
    public enum Status {LOGIN, NEEDS_SIGN_UP, NEEDS_LINKING}

    /**
     * 분기 판정은 서비스가 끝냈다 — 여기서는 읽기 계약을 표현 계약으로 옮겨 담기만 한다.
     * {@code jwt}는 LOGIN일 때만 채워지므로 null을 그대로 통과시킨다({@code @JsonInclude(NON_NULL)}이
     * 직렬화에서 생략하는 것이 계약이다).
     */
    public static AuthSocialLoginResponse from(SocialLoginResult result) {
        return new AuthSocialLoginResponse(
            Status.valueOf(result.status().name()),
            result.tempToken(),
            result.jwt() == null ? null : AuthJwtResponse.from(result.jwt())
        );
    }
}
