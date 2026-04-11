package com.tastyhouse.webapi.auth.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "카카오 계정 연동 응답. status 필드로 분기: LOGIN / NEEDS_SIGN_UP")
public record KakaoLinkResponse(

    @Schema(description = "처리 결과 상태. LOGIN=연동 및 로그인 성공, NEEDS_SIGN_UP=해당 전화번호로 가입된 계정 없어 신규 회원가입 필요")
    Status status,

    @Schema(description = "카카오 임시 토큰 (10분 유효). NEEDS_SIGN_UP 시 반환. 회원가입 API 호출에 사용하며 1회 사용 후 폐기됩니다.")
    String kakaoTempToken,

    @Schema(description = "앱 JWT 토큰 정보. status=LOGIN일 때만 반환")
    JwtResponse jwt,

    @Schema(description = "카카오 프로필 정보. status=NEEDS_SIGN_UP일 때 반환")
    KakaoProfile kakaoProfile
) {
    public enum Status { LOGIN, NEEDS_SIGN_UP }

    public record KakaoProfile(
        @Schema(description = "카카오 사용자 ID (소셜 계정 식별용)")
        String providerId,

        @Schema(description = "카카오 이메일 (회원가입 아이디로 사용 가능)")
        String email,

        @Schema(description = "카카오 닉네임")
        String nickname,

        @Schema(description = "카카오 프로필 이미지 URL")
        String profileImageUrl,

        @Schema(description = "카카오 실명 (회원가입 이름 자동 입력용)")
        String name,

        @Schema(description = "카카오 전화번호 (배달/예약 연락처 자동 입력용)")
        String phoneNumber
    ) {}

    public static KakaoLinkResponse ofLogin(JwtResponse jwt) {
        return new KakaoLinkResponse(Status.LOGIN, null, jwt, null);
    }

    public static KakaoLinkResponse ofSignUpRequired(String kakaoTempToken,
                                                     String providerId, String email,
                                                     String nickname, String profileImageUrl,
                                                     String name, String phoneNumber) {
        return new KakaoLinkResponse(Status.NEEDS_SIGN_UP, kakaoTempToken, null,
            new KakaoProfile(providerId, email, nickname, profileImageUrl, name, phoneNumber));
    }
}
