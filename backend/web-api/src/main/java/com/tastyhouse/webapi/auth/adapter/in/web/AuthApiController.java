package com.tastyhouse.webapi.auth.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.port.in.MemberLoginUseCase;
import com.tastyhouse.application.auth.port.in.MemberLogoutUseCase;
import com.tastyhouse.application.auth.port.in.MemberPhoneLoginUseCase;
import com.tastyhouse.application.auth.port.in.MemberRefreshUseCase;
import com.tastyhouse.application.auth.port.in.MemberSignUpUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.ratelimit.RateLimit;
import com.tastyhouse.apicommon.ratelimit.RateLimitKeyType;
import com.tastyhouse.webapi.auth.adapter.in.web.request.LoginRequest;
import com.tastyhouse.webapi.auth.adapter.in.web.request.PhoneLoginRequest;
import com.tastyhouse.webapi.auth.adapter.in.web.request.RefreshTokenRequest;
import com.tastyhouse.webapi.auth.adapter.in.web.request.SignUpRequest;
import com.tastyhouse.webapi.auth.adapter.in.web.response.AuthJwtResponse;
import com.tastyhouse.webapi.auth.adapter.in.web.response.AuthPhoneLoginResponse;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증 관련 API")
class AuthApiController {

    private final MemberSignUpUseCase memberSignUpUseCase;
    private final MemberLoginUseCase memberLoginUseCase;
    private final MemberRefreshUseCase memberRefreshUseCase;
    private final MemberLogoutUseCase memberLogoutUseCase;
    private final MemberPhoneLoginUseCase memberPhoneLoginUseCase;

    public AuthApiController(
        MemberSignUpUseCase memberSignUpUseCase,
        MemberLoginUseCase memberLoginUseCase,
        MemberRefreshUseCase memberRefreshUseCase,
        MemberLogoutUseCase memberLogoutUseCase,
        MemberPhoneLoginUseCase memberPhoneLoginUseCase
    ) {
        this.memberSignUpUseCase = memberSignUpUseCase;
        this.memberLoginUseCase = memberLoginUseCase;
        this.memberRefreshUseCase = memberRefreshUseCase;
        this.memberLogoutUseCase = memberLogoutUseCase;
        this.memberPhoneLoginUseCase = memberPhoneLoginUseCase;
    }

    @Operation(summary = "회원가입", description = "새 회원을 등록합니다. 휴대폰번호 입력 시 SMS 인증(smsVerifyToken)이 필요합니다. 생성된 회원의 식별자(id)를 반환합니다.")
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:signup")
    @PostMapping("/v1/signup")
    public ResponseEntity<ApiResponse<Long>> signUp(@Valid @RequestBody SignUpRequest request) {
        Long memberId = memberSignUpUseCase.signUp(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(memberId));
    }

    @Operation(summary = "로그인", description = "사용자 인증을 통해 JWT 토큰을 발급합니다.")
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:login")
    @PostMapping("/v1/login")
    public ResponseEntity<ApiResponse<AuthJwtResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(ApiResponse.success(AuthJwtResponse.from(memberLoginUseCase.login(loginRequest.username(), loginRequest.password(), loginRequest.rememberMe()))));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급합니다.")
    @PostMapping("/v1/refresh")
    public ResponseEntity<ApiResponse<AuthJwtResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success(AuthJwtResponse.from(memberRefreshUseCase.refresh(request.refreshToken()))));
    }

    @Operation(summary = "로그아웃", description = "Access Token을 블랙리스트에 등록하고 Refresh Token을 삭제하여 로그아웃 처리합니다.")
    @PostMapping("/v1/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String bearerToken) {
        memberLogoutUseCase.logout(bearerToken);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "휴대폰 인증 로그인", description = "휴대폰 인증 완료 후 발급된 smsVerifyToken으로 로그인합니다. 기존 회원이면 JWT를 발급하고, 신규 사용자이면 needsSignUp=true를 반환합니다.")
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:phone_login")
    @PostMapping("/v1/login/phone")
    public ResponseEntity<ApiResponse<AuthPhoneLoginResponse>> phoneLogin(@Valid @RequestBody PhoneLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(AuthPhoneLoginResponse.from(memberPhoneLoginUseCase.phoneLogin(request.smsVerifyToken()))));
    }
}
