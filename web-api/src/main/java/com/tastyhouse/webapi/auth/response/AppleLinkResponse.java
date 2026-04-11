package com.tastyhouse.webapi.auth.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "애플 계정 연동 응답. status 필드로 분기: LOGIN / NEEDS_SIGN_UP")
public record AppleLinkResponse(

    @Schema(description = "처리 결과 상태. LOGIN=연동 및 로그인 성공, NEEDS_SIGN_UP=해당 전화번호로 가입된 계정 없어 신규 회원가입 필요")
    Status status,

    @Schema(description = "애플 임시 토큰 (10분 유효). NEEDS_SIGN_UP 시 반환. 회원가입 API 호출에 사용하며 1회 사용 후 폐기됩니다.")
    String appleTempToken,

    @Schema(description = "앱 JWT 토큰 정보. status=LOGIN일 때만 반환")
    JwtResponse jwt,

    @Schema(description = "애플 프로필 정보. status=NEEDS_SIGN_UP일 때 반환")
    AppleProfile appleProfile
) {
    public enum Status { LOGIN, NEEDS_SIGN_UP }

    public record AppleProfile(
        @Schema(description = "Apple sub (소셜 계정 식별용)")
        String providerId,

        @Schema(description = "Apple 이메일 (Private Relay 주소일 수 있음)")
        String email
    ) {}

    public static AppleLinkResponse ofLogin(JwtResponse jwt) {
        return new AppleLinkResponse(Status.LOGIN, null, jwt, null);
    }

    public static AppleLinkResponse ofSignUpRequired(String appleTempToken, String providerId, String email) {
        return new AppleLinkResponse(Status.NEEDS_SIGN_UP, appleTempToken, null,
            new AppleProfile(providerId, email));
    }
}
