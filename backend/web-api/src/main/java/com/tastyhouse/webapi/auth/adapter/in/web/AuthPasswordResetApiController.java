package com.tastyhouse.webapi.auth.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.port.in.MemberAuthCommandUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.ratelimit.RateLimit;
import com.tastyhouse.apicommon.ratelimit.RateLimitKeyType;
import com.tastyhouse.webapi.auth.adapter.in.web.request.PasswordResetConfirmRequest;
import com.tastyhouse.webapi.auth.adapter.in.web.request.PasswordResetRequestRequest;
import com.tastyhouse.webapi.auth.adapter.in.web.request.PasswordResetVerifyRequest;
import com.tastyhouse.webapi.auth.adapter.in.web.response.AuthPasswordResetTokenResponse;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth Password Reset", description = "비밀번호 재설정 API")
public class AuthPasswordResetApiController {

    private final MemberAuthCommandUseCase authCommandUseCase;

    public AuthPasswordResetApiController(MemberAuthCommandUseCase authCommandUseCase) {
        this.authCommandUseCase = authCommandUseCase;
    }

    @Operation(summary = "비밀번호 찾기 - 인증코드 발송", description = "아이디(이메일)로 비밀번호 재설정 인증코드를 발송합니다. 가입되지 않은 아이디도 동일한 응답을 반환합니다.")
    @RateLimit(limit = 5, windowSeconds = 60, keyType = RateLimitKeyType.IP, keyPrefix = "rate_limit:password_reset_request")
    @PostMapping("/v1/password-reset/request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequestRequest request) {
        authCommandUseCase.sendPasswordResetCode(request.username());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "비밀번호 찾기 - 인증코드 확인", description = "인증코드를 확인하고 비밀번호 재설정 토큰(15분 유효)을 발급합니다.")
    @PostMapping("/v1/password-reset/verify")
    public ResponseEntity<ApiResponse<AuthPasswordResetTokenResponse>> verifyPasswordReset(@Valid @RequestBody PasswordResetVerifyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(AuthPasswordResetTokenResponse.from(authCommandUseCase.verifyPasswordResetCode(request.username(), request.verificationCode()))));
    }

    @Operation(summary = "비밀번호 재설정", description = "비밀번호 재설정 토큰을 사용하여 새 비밀번호로 변경합니다.")
    @PostMapping("/v1/password-reset/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authCommandUseCase.resetPassword(request.passwordResetToken(), request.newPassword(), request.newPasswordConfirm());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
