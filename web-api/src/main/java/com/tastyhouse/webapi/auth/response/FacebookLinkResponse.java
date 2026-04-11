package com.tastyhouse.webapi.auth.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "페이스북 계정 연동 응답. status 필드로 분기: LOGIN / NEEDS_SIGN_UP")
public record FacebookLinkResponse(

    @Schema(description = "처리 결과 상태. LOGIN=연동 및 로그인 성공, NEEDS_SIGN_UP=해당 전화번호로 가입된 계정 없어 신규 회원가입 필요")
    Status status,

    @Schema(description = "페이스북 임시 토큰 (10분 유효). NEEDS_SIGN_UP 시 반환. 회원가입 API 호출에 사용하며 1회 사용 후 폐기됩니다.")
    String facebookTempToken,

    @Schema(description = "앱 JWT 토큰 정보. status=LOGIN일 때만 반환")
    JwtResponse jwt,

    @Schema(description = "페이스북 프로필 정보. status=NEEDS_SIGN_UP일 때 반환")
    FacebookProfile facebookProfile
) {
    public enum Status { LOGIN, NEEDS_SIGN_UP }

    public record FacebookProfile(
        @Schema(description = "페이스북 사용자 ID (소셜 계정 식별용)")
        String providerId,

        @Schema(description = "페이스북 이메일 (회원가입 아이디로 사용 가능)")
        String email,

        @Schema(description = "페이스북 이름")
        String name,

        @Schema(description = "페이스북 프로필 이미지 URL")
        String profileImageUrl
    ) {}

    public static FacebookLinkResponse ofLogin(JwtResponse jwt) {
        return new FacebookLinkResponse(Status.LOGIN, null, jwt, null);
    }

    public static FacebookLinkResponse ofSignUpRequired(String facebookTempToken,
                                                        String providerId, String email,
                                                        String name, String profileImageUrl) {
        return new FacebookLinkResponse(Status.NEEDS_SIGN_UP, facebookTempToken, null,
            new FacebookProfile(providerId, email, name, profileImageUrl));
    }
}
