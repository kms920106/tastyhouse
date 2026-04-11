package com.tastyhouse.webapi.auth.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "페이스북 로그인 응답. status 필드로 분기: LOGIN / NEEDS_SIGN_UP / NEEDS_LINKING")
public record FacebookLoginResponse(

    @Schema(description = "처리 결과 상태. LOGIN=로그인 성공, NEEDS_SIGN_UP=신규 회원가입 필요, NEEDS_LINKING=기존 일반가입 계정과 소셜 연동 필요")
    Status status,

    @Schema(description = "페이스북 임시 토큰 (10분 유효). NEEDS_SIGN_UP/NEEDS_LINKING 시 반환. 회원가입·연동 API 호출에 사용하며 1회 사용 후 폐기됩니다.")
    String facebookTempToken,

    @Schema(description = "앱 JWT 토큰 정보. status=LOGIN일 때만 반환")
    JwtResponse jwt
) {
    public enum Status { LOGIN, NEEDS_SIGN_UP, NEEDS_LINKING }

    public static FacebookLoginResponse ofLogin(JwtResponse jwt) {
        return new FacebookLoginResponse(Status.LOGIN, null, jwt);
    }

    public static FacebookLoginResponse ofSignUpRequired(String facebookTempToken) {
        return new FacebookLoginResponse(Status.NEEDS_SIGN_UP, facebookTempToken, null);
    }

    public static FacebookLoginResponse ofLinkingRequired(String facebookTempToken) {
        return new FacebookLoginResponse(Status.NEEDS_LINKING, facebookTempToken, null);
    }
}
