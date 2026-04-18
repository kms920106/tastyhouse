package com.tastyhouse.webapi.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "휴대폰 인증 로그인 응답")
public record PhoneLoginResponse(

    @Schema(description = "회원가입 필요 여부. true이면 /api/auth/v1/signup/kakao 또는 /api/auth/v1/signup으로 가입 완료 필요")
    boolean needsSignUp,

    @Schema(description = "JWT 토큰 정보. needsSignUp=false일 때만 반환")
    JwtResponse jwt
) {
    public static PhoneLoginResponse ofLogin(JwtResponse jwt) {
    return new PhoneLoginResponse(false, jwt);
    }

    public static PhoneLoginResponse ofSignUpRequired() {
    return new PhoneLoginResponse(true, null);
    }
}
