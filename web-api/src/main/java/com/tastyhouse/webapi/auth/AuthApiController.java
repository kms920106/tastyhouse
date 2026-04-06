package com.tastyhouse.webapi.auth;

import com.tastyhouse.core.common.CommonResponse;
import com.tastyhouse.webapi.auth.request.LoginRequest;
import com.tastyhouse.webapi.auth.request.PasswordResetConfirmRequest;
import com.tastyhouse.webapi.auth.request.PasswordResetRequestRequest;
import com.tastyhouse.webapi.auth.request.PasswordResetVerifyRequest;
import com.tastyhouse.webapi.auth.request.RefreshTokenRequest;
import com.tastyhouse.webapi.auth.request.SignUpRequest;
import com.tastyhouse.webapi.auth.response.JwtResponse;
import com.tastyhouse.webapi.auth.response.PasswordResetTokenResponse;
import com.tastyhouse.webapi.ratelimit.RateLimit;
import com.tastyhouse.webapi.ratelimit.RateLimitKeyType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthApiController {

    private final AuthFacade authFacade;

    @Operation(summary = "회원가입", description = "새 회원을 등록합니다. 휴대폰번호 입력 시 SMS 인증(phoneVerifyToken)이 필요합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원가입 성공", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "400", description = "유효성 검증 실패 또는 인증 토큰 오류", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "409", description = "아이디 또는 닉네임 중복", content = @Content(schema = @Schema(hidden = true)))
    })
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:signup")
    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<Void>> signUp(@Valid @RequestBody SignUpRequest request) {
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
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(null));
    }

    @Operation(summary = "로그인", description = "사용자 인증을 통해 JWT 토큰을 발급합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = JwtResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패 (아이디 또는 비밀번호 불일치)", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "429", description = "요청 횟수 초과 (IP당 분당 10회)", content = @Content(schema = @Schema(hidden = true)))
    })
    @RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:login")
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<JwtResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(CommonResponse.success(authFacade.login(loginRequest.username(), loginRequest.password(), loginRequest.rememberMe())));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 갱신 성공", content = @Content(schema = @Schema(implementation = JwtResponse.class))),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse<JwtResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(CommonResponse.success(authFacade.refresh(request.refreshToken())));
    }

    @Operation(summary = "로그아웃", description = "Access Token을 블랙리스트에 등록하고 Refresh Token을 삭제하여 로그아웃 처리합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그아웃 성공", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 토큰", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout(@RequestHeader("Authorization") String bearerToken) {
        authFacade.logout(bearerToken);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "비밀번호 찾기 - 인증코드 발송", description = "아이디(이메일)로 비밀번호 재설정 인증코드를 발송합니다. 가입되지 않은 아이디도 동일한 응답을 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "인증코드 발송 완료", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "400", description = "유효성 검증 실패", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "429", description = "요청 횟수 초과 (IP당 분당 5회)", content = @Content(schema = @Schema(hidden = true)))
    })
    @RateLimit(limit = 5, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:password_reset_request")
    @PostMapping("/password-reset/request")
    public ResponseEntity<CommonResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequestRequest request) {
        authFacade.sendPasswordResetCode(request.username());
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "비밀번호 찾기 - 인증코드 확인", description = "인증코드를 확인하고 비밀번호 재설정 토큰(15분 유효)을 발급합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "인증 성공, 비밀번호 재설정 토큰 발급", content = @Content(schema = @Schema(implementation = PasswordResetTokenResponse.class))),
        @ApiResponse(responseCode = "400", description = "인증코드 불일치 또는 만료", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/password-reset/verify")
    public ResponseEntity<CommonResponse<PasswordResetTokenResponse>> verifyPasswordReset(@Valid @RequestBody PasswordResetVerifyRequest request) {
        return ResponseEntity.ok(CommonResponse.success(authFacade.verifyPasswordResetCode(request.username(), request.verificationCode())));
    }

    @Operation(summary = "비밀번호 재설정", description = "비밀번호 재설정 토큰을 사용하여 새 비밀번호로 변경합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "400", description = "토큰 만료/무효, 비밀번호 불일치, 기존 비밀번호와 동일", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<CommonResponse<Void>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authFacade.resetPassword(request.passwordResetToken(), request.newPassword(), request.newPasswordConfirm());
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
