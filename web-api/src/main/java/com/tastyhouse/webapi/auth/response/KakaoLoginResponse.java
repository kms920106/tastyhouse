package com.tastyhouse.webapi.auth.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "카카오 로그인 응답. status 필드로 분기: LOGIN / NEEDS_SIGN_UP / NEEDS_LINKING")
public record KakaoLoginResponse(

    @Schema(description = "처리 결과 상태. LOGIN=로그인 성공, NEEDS_SIGN_UP=신규 회원가입 필요, NEEDS_LINKING=기존 일반가입 계정과 소셜 연동 필요")
    Status status,

    @Schema(description = "카카오 액세스 토큰. 모든 status에서 반환. NEEDS_SIGN_UP/NEEDS_LINKING 시 회원가입·연동 API 호출에 사용")
    String kakaoAccessToken,

    @Schema(description = "앱 JWT 토큰 정보. status=LOGIN일 때만 반환")
    JwtResponse jwt,

    @Schema(description = "카카오 프로필 정보. status=NEEDS_SIGN_UP 또는 NEEDS_LINKING일 때 반환")
    KakaoProfile kakaoProfile
) {
    public enum Status { LOGIN, NEEDS_SIGN_UP, NEEDS_LINKING }

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

    public static KakaoLoginResponse ofLogin(String kakaoAccessToken, JwtResponse jwt) {
        return new KakaoLoginResponse(Status.LOGIN, kakaoAccessToken, jwt, null);
    }

    public static KakaoLoginResponse ofSignUpRequired(String kakaoAccessToken,
                                                       String providerId, String email,
                                                       String nickname, String profileImageUrl,
                                                       String name, String phoneNumber) {
        return new KakaoLoginResponse(Status.NEEDS_SIGN_UP, kakaoAccessToken, null,
            new KakaoProfile(providerId, email, nickname, profileImageUrl, name, phoneNumber));
    }

    public static KakaoLoginResponse ofLinkingRequired(String kakaoAccessToken,
                                                        String providerId, String email,
                                                        String nickname, String profileImageUrl,
                                                        String name, String phoneNumber) {
        return new KakaoLoginResponse(Status.NEEDS_LINKING, kakaoAccessToken, null,
            new KakaoProfile(providerId, email, nickname, profileImageUrl, name, phoneNumber));
    }
}
