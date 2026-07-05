package com.tastyhouse.webapi.verification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.ratelimit.RateLimit;
import com.tastyhouse.webapi.ratelimit.RateLimitKeyType;
import com.tastyhouse.webapi.verification.request.ConfirmEmailVerificationCodeRequest;
import com.tastyhouse.webapi.verification.request.SendEmailVerificationCodeRequest;
import com.tastyhouse.webapi.verification.response.EmailVerifyTokenResponse;

@RestController
@RequestMapping("/api/email-verifications")
@RequiredArgsConstructor
@Tag(name = "Email Verification", description = "이메일 인증 API")
public class EmailVerificationApiController {

    private final EmailVerificationService emailVerificationService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(
        summary = "인증번호 발송",
        description = "입력한 이메일로 6자리 인증번호를 발송합니다. 기존 미완료 인증은 자동 만료됩니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증번호 발송 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 이메일 형식"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "요청 횟수 초과 (이메일당 일 5회)")
    })
    @RateLimit(limit = 5, windowSeconds = 86400, keyType = RateLimitKeyType.FIELD, keyField = "email", keyPrefix = "rate_limit:email_verification")
    @PostMapping("/v1/send")
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(
        @Valid @RequestBody SendEmailVerificationCodeRequest request
    ) {
        emailVerificationService.sendVerificationCode(request.email());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(
        summary = "인증번호 확인",
        description = "발송된 인증번호를 검증합니다. 인증 성공 시 10분간 유효한 emailVerifyToken을 반환합니다. " +
                      "회원가입 시 emailVerifyToken을 포함하여 사용합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 성공 - emailVerifyToken 반환",
            content = @Content(schema = @Schema(implementation = EmailVerifyTokenResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인증번호 불일치 또는 만료")
    })
    @PostMapping("/v1/confirm")
    public ResponseEntity<ApiResponse<EmailVerifyTokenResponse>> confirmVerificationCode(
        @Valid @RequestBody ConfirmEmailVerificationCodeRequest request
    ) {
        String email = emailVerificationService.confirmVerificationCode(request.email(), request.verificationCode());
        String emailVerifyToken = jwtTokenProvider.createEmailVerifyToken(email);
        return ResponseEntity.ok(ApiResponse.success(EmailVerifyTokenResponse.from(emailVerifyToken)));
    }
}
