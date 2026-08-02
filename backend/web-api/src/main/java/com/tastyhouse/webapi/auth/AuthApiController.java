package com.tastyhouse.webapi.auth;

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

import com.tastyhouse.security.ratelimit.RateLimit;
import com.tastyhouse.security.ratelimit.RateLimitKeyType;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.webapi.auth.request.AppleLoginRequest;
import com.tastyhouse.webapi.auth.request.FacebookLoginRequest;
import com.tastyhouse.webapi.auth.request.KakaoLoginRequest;
import com.tastyhouse.webapi.auth.request.LoginRequest;
import com.tastyhouse.webapi.auth.request.NaverLoginRequest;
import com.tastyhouse.webapi.auth.request.PasswordResetConfirmRequest;
import com.tastyhouse.webapi.auth.request.PasswordResetRequestRequest;
import com.tastyhouse.webapi.auth.request.PasswordResetVerifyRequest;
import com.tastyhouse.webapi.auth.request.PhoneLoginRequest;
import com.tastyhouse.webapi.auth.request.RefreshTokenRequest;
import com.tastyhouse.webapi.auth.request.SignUpRequest;
import com.tastyhouse.webapi.auth.request.SocialAccountLinkRequest;
import com.tastyhouse.webapi.auth.request.SocialSignUpRequest;
import com.tastyhouse.webapi.auth.response.AuthJwtResponse;
import com.tastyhouse.webapi.auth.response.AuthPasswordResetTokenResponse;
import com.tastyhouse.webapi.auth.response.AuthPhoneLoginResponse;
import com.tastyhouse.webapi.auth.response.AuthSocialLinkResponse;
import com.tastyhouse.webapi.auth.response.AuthSocialLoginResponse;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthApiController {

    private final AuthService authService;

    public AuthApiController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "회원가입", description = "새 회원을 등록합니다. 휴대폰번호 입력 시 SMS 인증(smsVerifyToken)이 필요합니다. 생성된 회원의 식별자(id)를 반환합니다.")
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:signup")
    @PostMapping("/v1/signup")
    public ResponseEntity<ApiResponse<Long>> signUp(@Valid @RequestBody SignUpRequest request) {
        Long memberId = authService.signUp(
            request.username(),
            request.password(),
            request.nickname(),
            request.fullName(),
            request.gender(),
            request.birthDate(),
            request.phoneNumber(),
            request.pushNotificationEnabled(),
            request.marketingInfoEnabled(),
            request.eventInfoEnabled(),
            request.smsVerifyToken(),
            request.mailVerifyToken(),
            request.referrerNickname()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(memberId));
    }

    @Operation(summary = "로그인", description = "사용자 인증을 통해 JWT 토큰을 발급합니다.")
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:login")
    @PostMapping("/v1/login")
    public ResponseEntity<ApiResponse<AuthJwtResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(loginRequest.username(), loginRequest.password(), loginRequest.rememberMe())));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급합니다.")
    @PostMapping("/v1/refresh")
    public ResponseEntity<ApiResponse<AuthJwtResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(request.refreshToken())));
    }

    @Operation(summary = "로그아웃", description = "Access Token을 블랙리스트에 등록하고 Refresh Token을 삭제하여 로그아웃 처리합니다.")
    @PostMapping("/v1/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String bearerToken) {
        authService.logout(bearerToken);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "비밀번호 찾기 - 인증코드 발송", description = "아이디(이메일)로 비밀번호 재설정 인증코드를 발송합니다. 가입되지 않은 아이디도 동일한 응답을 반환합니다.")
    @RateLimit(limit = 5, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:password_reset_request")
    @PostMapping("/v1/password-reset/request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequestRequest request) {
        authService.sendPasswordResetCode(request.username());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "비밀번호 찾기 - 인증코드 확인", description = "인증코드를 확인하고 비밀번호 재설정 토큰(15분 유효)을 발급합니다.")
    @PostMapping("/v1/password-reset/verify")
    public ResponseEntity<ApiResponse<AuthPasswordResetTokenResponse>> verifyPasswordReset(@Valid @RequestBody PasswordResetVerifyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.verifyPasswordResetCode(request.username(), request.verificationCode())));
    }

    @Operation(summary = "비밀번호 재설정", description = "비밀번호 재설정 토큰을 사용하여 새 비밀번호로 변경합니다.")
    @PostMapping("/v1/password-reset/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.resetPassword(request.passwordResetToken(), request.newPassword(), request.newPasswordConfirm());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "휴대폰 인증 로그인", description = "휴대폰 인증 완료 후 발급된 smsVerifyToken으로 로그인합니다. 기존 회원이면 JWT를 발급하고, 신규 사용자이면 needsSignUp=true를 반환합니다.")
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:phone_login")
    @PostMapping("/v1/login/phone")
    public ResponseEntity<ApiResponse<AuthPhoneLoginResponse>> phoneLogin(@Valid @RequestBody PhoneLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.phoneLogin(request.smsVerifyToken())));
    }

    @Operation(summary = "카카오 로그인", description = "카카오 인가 코드로 로그인합니다. 기존 회원이면 JWT를 발급하고, 신규 사용자이면 needsSignUp=true와 카카오 프로필 정보를 반환합니다.")
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:kakao_login")
    @PostMapping("/v1/login/kakao")
    public ResponseEntity<ApiResponse<AuthSocialLoginResponse>> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.kakaoLogin(request.code())));
    }

    @Operation(summary = "네이버 로그인", description = "네이버 인가 코드와 state로 로그인합니다. 기존 회원이면 JWT를 발급하고, 신규 사용자이면 needsSignUp=true와 네이버 프로필 정보를 반환합니다.")
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:naver_login")
    @PostMapping("/v1/login/naver")
    public ResponseEntity<ApiResponse<AuthSocialLoginResponse>> naverLogin(@Valid @RequestBody NaverLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.naverLogin(request.code(), request.state())));
    }

    @Operation(summary = "페이스북 로그인", description = "Facebook JS SDK로부터 발급받은 액세스 토큰으로 로그인합니다. 기존 회원이면 JWT를 발급하고, 신규 사용자이면 needsSignUp=true와 페이스북 프로필 정보를 반환합니다.")
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:facebook_login")
    @PostMapping("/v1/login/facebook")
    public ResponseEntity<ApiResponse<AuthSocialLoginResponse>> facebookLogin(@Valid @RequestBody FacebookLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.facebookLogin(request.accessToken())));
    }

    @Operation(summary = "애플 로그인", description = "Apple 인가 코드로 로그인합니다. 기존 회원이면 JWT를 발급하고, 신규 사용자이면 needsSignUp=true와 애플 프로필 정보를 반환합니다.")
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:apple_login")
    @PostMapping("/v1/login/apple")
    public ResponseEntity<ApiResponse<AuthSocialLoginResponse>> appleLogin(@Valid @RequestBody AppleLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.appleLogin(request.code())));
    }

    @Operation(summary = "소셜 계정 연동", description = "소셜 로그인 시 status=NEEDS_LINKING을 받은 경우, 휴대폰 인증(smsVerifyToken)으로 본인 확인 후 소셜 계정을 연동하고 JWT를 발급합니다. 해당 전화번호로 가입된 계정이 없으면 status=NEEDS_SIGN_UP을 반환합니다.")
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:social_link")
    @PostMapping("/v1/link/social")
    public ResponseEntity<ApiResponse<AuthSocialLinkResponse>> linkSocialAccount(@Valid @RequestBody SocialAccountLinkRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            authService.linkAccount(request.provider(), request.tempToken(), request.smsVerifyToken())
        ));
    }

    @Operation(summary = "소셜 회원가입", description = "소셜 임시 토큰과 추가 정보로 소셜 회원가입을 완료하고 JWT를 발급합니다.")
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:social_signup")
    @PostMapping("/v1/signup/social")
    public ResponseEntity<ApiResponse<AuthJwtResponse>> signUpSocialAccount(@Valid @RequestBody SocialSignUpRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
            authService.socialSignUp(
                request.provider(),
                request.tempToken(),
                request.username(),
                request.nickname(),
                request.fullName(),
                request.gender(),
                request.birthDate(),
                request.phoneNumber(),
                request.pushNotificationEnabled(),
                request.marketingInfoEnabled(),
                request.eventInfoEnabled(),
                request.referrerNickname()
            )
        ));
    }
}
