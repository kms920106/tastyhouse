package com.tastyhouse.webapi.auth.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.auth.port.out.PhoneLoginResult;

@Schema(description = "휴대폰 인증 로그인 응답")
public record AuthPhoneLoginResponse(

    @Schema(description = "회원가입 필요 여부. true이면 /api/auth/v1/signup/kakao 또는 /api/auth/v1/signup으로 가입 완료 필요")
    boolean needsSignUp,

    @Schema(description = "JWT 토큰 정보. needsSignUp=false일 때만 반환")
    AuthJwtResponse jwt
) {

    public static AuthPhoneLoginResponse from(PhoneLoginResult result) {
        return new AuthPhoneLoginResponse(
            result.needsSignUp(),
            result.jwt() == null ? null : AuthJwtResponse.from(result.jwt())
        );
    }
}
