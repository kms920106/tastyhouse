package com.tastyhouse.webapi.auth.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.port.in.MemberAuthCommandUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.ratelimit.RateLimit;
import com.tastyhouse.apicommon.ratelimit.RateLimitKeyType;
import com.tastyhouse.webapi.auth.adapter.in.web.request.AppleLoginRequest;
import com.tastyhouse.webapi.auth.adapter.in.web.request.FacebookLoginRequest;
import com.tastyhouse.webapi.auth.adapter.in.web.request.KakaoLoginRequest;
import com.tastyhouse.webapi.auth.adapter.in.web.request.NaverLoginRequest;
import com.tastyhouse.webapi.auth.adapter.in.web.request.SocialAccountLinkRequest;
import com.tastyhouse.webapi.auth.adapter.in.web.request.SocialSignUpRequest;
import com.tastyhouse.webapi.auth.adapter.in.web.response.AuthJwtResponse;
import com.tastyhouse.webapi.auth.adapter.in.web.response.AuthSocialLinkResponse;
import com.tastyhouse.webapi.auth.adapter.in.web.response.AuthSocialLoginResponse;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth Social", description = "소셜 인증 API")
public class AuthSocialApiController {

    private final MemberAuthCommandUseCase authCommandUseCase;

    public AuthSocialApiController(MemberAuthCommandUseCase authCommandUseCase) {
        this.authCommandUseCase = authCommandUseCase;
    }

    @Operation(summary = "카카오 로그인", description = "카카오 인가 코드로 로그인합니다. 기존 회원이면 JWT를 발급하고, 신규 사용자이면 needsSignUp=true와 카카오 프로필 정보를 반환합니다.")
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:kakao_login")
    @PostMapping("/v1/login/kakao")
    public ResponseEntity<ApiResponse<AuthSocialLoginResponse>> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(AuthSocialLoginResponse.from(authCommandUseCase.kakaoLogin(request.code()))));
    }

    @Operation(summary = "네이버 로그인", description = "네이버 인가 코드와 state로 로그인합니다. 기존 회원이면 JWT를 발급하고, 신규 사용자이면 needsSignUp=true와 네이버 프로필 정보를 반환합니다.")
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:naver_login")
    @PostMapping("/v1/login/naver")
    public ResponseEntity<ApiResponse<AuthSocialLoginResponse>> naverLogin(@Valid @RequestBody NaverLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(AuthSocialLoginResponse.from(authCommandUseCase.naverLogin(request.code(), request.state()))));
    }

    @Operation(summary = "페이스북 로그인", description = "Facebook JS SDK로부터 발급받은 액세스 토큰으로 로그인합니다. 기존 회원이면 JWT를 발급하고, 신규 사용자이면 needsSignUp=true와 페이스북 프로필 정보를 반환합니다.")
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:facebook_login")
    @PostMapping("/v1/login/facebook")
    public ResponseEntity<ApiResponse<AuthSocialLoginResponse>> facebookLogin(@Valid @RequestBody FacebookLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(AuthSocialLoginResponse.from(authCommandUseCase.facebookLogin(request.accessToken()))));
    }

    @Operation(summary = "애플 로그인", description = "Apple 인가 코드로 로그인합니다. 기존 회원이면 JWT를 발급하고, 신규 사용자이면 needsSignUp=true와 애플 프로필 정보를 반환합니다.")
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:apple_login")
    @PostMapping("/v1/login/apple")
    public ResponseEntity<ApiResponse<AuthSocialLoginResponse>> appleLogin(@Valid @RequestBody AppleLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(AuthSocialLoginResponse.from(authCommandUseCase.appleLogin(request.code()))));
    }

    @Operation(summary = "소셜 계정 연동", description = "소셜 로그인 시 status=NEEDS_LINKING을 받은 경우, 휴대폰 인증(smsVerifyToken)으로 본인 확인 후 소셜 계정을 연동하고 JWT를 발급합니다. 해당 전화번호로 가입된 계정이 없으면 status=NEEDS_SIGN_UP을 반환합니다.")
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:social_link")
    @PostMapping("/v1/link/social")
    public ResponseEntity<ApiResponse<AuthSocialLinkResponse>> linkSocialAccount(@Valid @RequestBody SocialAccountLinkRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            AuthSocialLinkResponse.from(authCommandUseCase.linkAccount(request.provider(), request.tempToken(), request.smsVerifyToken()))
        ));
    }

    @Operation(summary = "소셜 회원가입", description = "소셜 임시 토큰과 추가 정보로 소셜 회원가입을 완료하고 JWT를 발급합니다.")
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:social_signup")
    @PostMapping("/v1/signup/social")
    public ResponseEntity<ApiResponse<AuthJwtResponse>> signUpSocialAccount(@Valid @RequestBody SocialSignUpRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
            AuthJwtResponse.from(authCommandUseCase.socialSignUp(request.toCommand()))
        ));
    }
}
