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

import com.tastyhouse.core.domain.verification.application.PhoneVerificationCommandService;
import com.tastyhouse.core.domain.verification.application.dto.command.ConfirmPhoneVerificationCommand;
import com.tastyhouse.core.domain.verification.application.dto.command.SendPhoneVerificationCommand;
import com.tastyhouse.core.domain.verification.application.dto.result.PhoneVerificationResult;
import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.ratelimit.RateLimit;
import com.tastyhouse.webapi.ratelimit.RateLimitKeyType;
import com.tastyhouse.webapi.verification.request.ConfirmVerificationCodeRequest;
import com.tastyhouse.webapi.verification.request.SendVerificationCodeRequest;
import com.tastyhouse.webapi.verification.response.PhoneVerifyTokenResponse;

@RestController
@RequestMapping("/api/phone-verifications")
@RequiredArgsConstructor
@Tag(name = "Phone Verification", description = "휴대폰번호 인증 API")
public class PhoneVerificationApiController {

    private final PhoneVerificationCommandService phoneVerificationCommandService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(
        summary = "인증번호 발송",
        description = "입력한 휴대폰번호로 6자리 인증번호를 SMS 발송합니다. 기존 미완료 인증은 자동 만료됩니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증번호 발송 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 휴대폰번호 형식"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "요청 횟수 초과 (전화번호당 일 5회)")
    })
    @RateLimit(limit = 5, windowSeconds = 86400, keyType = RateLimitKeyType.FIELD, keyField = "phoneNumber", keyPrefix = "rate_limit:sms_verification")
    @PostMapping("/v1/send")
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(
        @Valid @RequestBody SendVerificationCodeRequest request
    ) {
        phoneVerificationCommandService.sendVerificationCode(
            new SendPhoneVerificationCommand(request.phoneNumber())
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(
        summary = "인증번호 확인",
        description = "발송된 인증번호를 검증합니다. 인증 성공 시 10분간 유효한 phoneVerifyToken을 반환합니다. " +
                      "개인정보 수정(휴대폰번호 변경) 시 X-Phone-Verify-Token 헤더에 포함하여 사용합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 성공 - phoneVerifyToken 반환",
            content = @Content(schema = @Schema(implementation = PhoneVerifyTokenResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인증번호 불일치 또는 만료"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping("/v1/confirm")
    public ResponseEntity<ApiResponse<PhoneVerifyTokenResponse>> confirmVerificationCode(
        @Valid @RequestBody ConfirmVerificationCodeRequest request
    ) {
        PhoneVerificationResult result = phoneVerificationCommandService.confirmVerificationCode(
            new ConfirmPhoneVerificationCommand(request.phoneNumber(), request.verificationCode())
        );
        String phoneVerifyToken = jwtTokenProvider.createPhoneVerifyToken(result.phoneNumber());
        return ResponseEntity.ok(ApiResponse.success(
            PhoneVerifyTokenResponse.from(phoneVerifyToken)
        ));
    }
}
