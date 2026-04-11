package com.tastyhouse.webapi.auth.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "네이버 계정 연동 응답. status 필드로 분기: LOGIN / NEEDS_SIGN_UP")
public record NaverLinkResponse(

    @Schema(description = "처리 결과 상태. LOGIN=연동 및 로그인 성공, NEEDS_SIGN_UP=해당 전화번호로 가입된 계정 없어 신규 회원가입 필요")
    Status status,

    @Schema(description = "네이버 임시 토큰 (10분 유효). NEEDS_SIGN_UP 시 반환. 회원가입 API 호출에 사용하며 1회 사용 후 폐기됩니다.")
    String naverTempToken,

    @Schema(description = "앱 JWT 토큰 정보. status=LOGIN일 때만 반환")
    JwtResponse jwt,

    @Schema(description = "네이버 프로필 정보. status=NEEDS_SIGN_UP일 때 반환")
    NaverProfile naverProfile
) {
    public enum Status { LOGIN, NEEDS_SIGN_UP }

    public record NaverProfile(
        @Schema(description = "네이버 사용자 ID (소셜 계정 식별용)")
        String providerId,

        @Schema(description = "네이버 이메일 (회원가입 아이디로 사용 가능)")
        String email,

        @Schema(description = "네이버 닉네임")
        String nickname,

        @Schema(description = "네이버 프로필 이미지 URL")
        String profileImageUrl,

        @Schema(description = "네이버 실명 (회원가입 이름 자동 입력용)")
        String name,

        @Schema(description = "네이버 휴대폰번호 (배달/예약 연락처 자동 입력용)")
        String phoneNumber
    ) {}

    public static NaverLinkResponse ofLogin(JwtResponse jwt) {
        return new NaverLinkResponse(Status.LOGIN, null, jwt, null);
    }

    public static NaverLinkResponse ofSignUpRequired(String naverTempToken,
                                                     String providerId, String email,
                                                     String nickname, String profileImageUrl,
                                                     String name, String phoneNumber) {
        return new NaverLinkResponse(Status.NEEDS_SIGN_UP, naverTempToken, null,
            new NaverProfile(providerId, email, nickname, profileImageUrl, name, phoneNumber));
    }
}
