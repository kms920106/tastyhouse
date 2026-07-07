package com.tastyhouse.webapi.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.webapi.ratelimit.RateLimit;
import com.tastyhouse.webapi.ratelimit.RateLimitKeyType;
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
import com.tastyhouse.webapi.auth.response.JwtResponse;
import com.tastyhouse.webapi.auth.response.PasswordResetTokenResponse;
import com.tastyhouse.webapi.auth.response.PhoneLoginResponse;
import com.tastyhouse.webapi.auth.response.SocialLinkResponse;
import com.tastyhouse.webapi.auth.response.SocialLoginResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthApiController {

    private final AuthFacade authFacade;

    @Operation(summary = "회원가입", description = "새 회원을 등록합니다. 휴대폰번호 입력 시 SMS 인증(phoneVerifyToken)이 필요합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공", content = @Content(schema = @Schema(hidden = true))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검증 실패 또는 인증 토큰 오류", content = @Content(schema = @Schema(hidden = true))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "아이디 또는 닉네임 중복", content = @Content(schema = @Schema(hidden = true)))
    })
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:signup")
    @PostMapping("/v1/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(@Valid @RequestBody SignUpRequest request) {
        authFacade.signUp(
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
            request.phoneVerifyToken(),
            request.emailVerifyToken(),
            request.referrerNickname()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    @Operation(summary = "로그인", description = "사용자 인증을 통해 JWT 토큰을 발급합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = JwtResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (아이디 또는 비밀번호 불일치)", content = @Content(schema = @Schema(hidden = true))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "요청 횟수 초과 (IP당 분당 10회)", content = @Content(schema = @Schema(hidden = true)))
    })
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:login")
    @PostMapping("/v1/login")
    public ResponseEntity<ApiResponse<JwtResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(ApiResponse.success(authFacade.login(loginRequest.username(), loginRequest.password(), loginRequest.rememberMe())));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 갱신 성공", content = @Content(schema = @Schema(implementation = JwtResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/v1/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authFacade.refresh(request.refreshToken())));
    }

    @Operation(summary = "로그아웃", description = "Access Token을 블랙리스트에 등록하고 Refresh Token을 삭제하여 로그아웃 처리합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공", content = @Content(schema = @Schema(hidden = true))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 토큰", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/v1/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String bearerToken) {
        authFacade.logout(bearerToken);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "비밀번호 찾기 - 인증코드 발송", description = "아이디(이메일)로 비밀번호 재설정 인증코드를 발송합니다. 가입되지 않은 아이디도 동일한 응답을 반환합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증코드 발송 완료", content = @Content(schema = @Schema(hidden = true))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검증 실패", content = @Content(schema = @Schema(hidden = true))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "요청 횟수 초과 (IP당 분당 5회)", content = @Content(schema = @Schema(hidden = true)))
    })
    @RateLimit(limit = 5, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:password_reset_request")
    @PostMapping("/v1/password-reset/request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequestRequest request) {
        authFacade.sendPasswordResetCode(request.username());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "비밀번호 찾기 - 인증코드 확인", description = "인증코드를 확인하고 비밀번호 재설정 토큰(15분 유효)을 발급합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 성공, 비밀번호 재설정 토큰 발급", content = @Content(schema = @Schema(implementation = PasswordResetTokenResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인증코드 불일치 또는 만료", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/v1/password-reset/verify")
    public ResponseEntity<ApiResponse<PasswordResetTokenResponse>> verifyPasswordReset(@Valid @RequestBody PasswordResetVerifyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authFacade.verifyPasswordResetCode(request.username(), request.verificationCode())));
    }

    @Operation(summary = "비밀번호 재설정", description = "비밀번호 재설정 토큰을 사용하여 새 비밀번호로 변경합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공", content = @Content(schema = @Schema(hidden = true))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "토큰 만료/무효, 비밀번호 불일치, 기존 비밀번호와 동일", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/v1/password-reset/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authFacade.resetPassword(request.passwordResetToken(), request.newPassword(), request.newPasswordConfirm());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "휴대폰 인증 로그인", description = "휴대폰 인증 완료 후 발급된 phoneVerifyToken으로 로그인합니다. 기존 회원이면 JWT를 발급하고, 신규 사용자이면 needsSignUp=true를 반환합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공 또는 회원가입 필요", content = @Content(schema = @Schema(implementation = PhoneLoginResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "phoneVerifyToken 만료 또는 유효하지 않음", content = @Content(schema = @Schema(hidden = true)))
    })
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:phone_login")
    @PostMapping("/v1/login/phone")
    public ResponseEntity<ApiResponse<PhoneLoginResponse>> phoneLogin(@Valid @RequestBody PhoneLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authFacade.phoneLogin(request.phoneVerifyToken())));
    }

    @Operation(summary = "카카오 로그인", description = "카카오 인가 코드로 로그인합니다. 기존 회원이면 JWT를 발급하고, 신규 사용자이면 needsSignUp=true와 카카오 프로필 정보를 반환합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공 또는 회원가입 필요", content = @Content(schema = @Schema(implementation = SocialLoginResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인가 코드 누락 또는 이메일 동의 미완료", content = @Content(schema = @Schema(hidden = true))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "카카오 서버 오류", content = @Content(schema = @Schema(hidden = true)))
    })
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:kakao_login")
    @PostMapping("/v1/login/kakao")
    public ResponseEntity<ApiResponse<SocialLoginResponse>> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authFacade.kakaoLogin(request.code())));
    }

    @Operation(summary = "네이버 로그인", description = "네이버 인가 코드와 state로 로그인합니다. 기존 회원이면 JWT를 발급하고, 신규 사용자이면 needsSignUp=true와 네이버 프로필 정보를 반환합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공 또는 회원가입 필요", content = @Content(schema = @Schema(implementation = SocialLoginResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인가 코드 또는 state 누락", content = @Content(schema = @Schema(hidden = true))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "네이버 서버 오류", content = @Content(schema = @Schema(hidden = true)))
    })
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:naver_login")
    @PostMapping("/v1/login/naver")
    public ResponseEntity<ApiResponse<SocialLoginResponse>> naverLogin(@Valid @RequestBody NaverLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authFacade.naverLogin(request.code(), request.state())));
    }

    @Operation(summary = "페이스북 로그인", description = "Facebook JS SDK로부터 발급받은 액세스 토큰으로 로그인합니다. 기존 회원이면 JWT를 발급하고, 신규 사용자이면 needsSignUp=true와 페이스북 프로필 정보를 반환합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공 또는 회원가입 필요", content = @Content(schema = @Schema(implementation = SocialLoginResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "액세스 토큰 누락 또는 유효하지 않음", content = @Content(schema = @Schema(hidden = true))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "페이스북 서버 오류", content = @Content(schema = @Schema(hidden = true)))
    })
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:facebook_login")
    @PostMapping("/v1/login/facebook")
    public ResponseEntity<ApiResponse<SocialLoginResponse>> facebookLogin(@Valid @RequestBody FacebookLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authFacade.facebookLogin(request.accessToken())));
    }

    @Operation(summary = "애플 로그인", description = "Apple 인가 코드로 로그인합니다. 기존 회원이면 JWT를 발급하고, 신규 사용자이면 needsSignUp=true와 애플 프로필 정보를 반환합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공 또는 회원가입 필요", content = @Content(schema = @Schema(implementation = SocialLoginResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인가 코드 누락 또는 id_token 검증 실패", content = @Content(schema = @Schema(hidden = true))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "Apple 서버 오류", content = @Content(schema = @Schema(hidden = true)))
    })
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:apple_login")
    @PostMapping("/v1/login/apple")
    public ResponseEntity<ApiResponse<SocialLoginResponse>> appleLogin(@Valid @RequestBody AppleLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authFacade.appleLogin(request.code())));
    }

    @Operation(summary = "소셜 계정 연동", description = "소셜 로그인 시 status=NEEDS_LINKING을 받은 경우, 휴대폰 인증(phoneVerifyToken)으로 본인 확인 후 소셜 계정을 연동하고 JWT를 발급합니다. 해당 전화번호로 가입된 계정이 없으면 status=NEEDS_SIGN_UP을 반환합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "연동 성공(status=LOGIN, JWT 발급) 또는 신규 회원가입 필요(status=NEEDS_SIGN_UP)", content = @Content(schema = @Schema(implementation = SocialLinkResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "provider 누락, phoneVerifyToken 만료 또는 유효하지 않음", content = @Content(schema = @Schema(hidden = true))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 연동된 소셜 계정", content = @Content(schema = @Schema(hidden = true)))
    })
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:social_link")
    @PostMapping("/v1/link/social")
    public ResponseEntity<ApiResponse<SocialLinkResponse>> linkSocialAccount(@Valid @RequestBody SocialAccountLinkRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            authFacade.linkAccount(request.provider(), request.tempToken(), request.phoneVerifyToken())
        ));
    }

    @Operation(summary = "소셜 회원가입", description = "소셜 임시 토큰과 추가 정보로 소셜 회원가입을 완료하고 JWT를 발급합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공", content = @Content(schema = @Schema(implementation = JwtResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "provider 누락, 유효성 검증 실패 또는 임시 토큰 만료", content = @Content(schema = @Schema(hidden = true))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 가입된 소셜 계정, 닉네임/아이디 중복", content = @Content(schema = @Schema(hidden = true)))
    })
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:social_signup")
    @PostMapping("/v1/signup/social")
    public ResponseEntity<ApiResponse<JwtResponse>> signUpSocialAccount(@Valid @RequestBody SocialSignUpRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
            authFacade.socialSignUp(
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
