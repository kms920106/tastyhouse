package com.tastyhouse.webapi.phoneverification.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.token.MemberJwtTokenProvider;
import com.tastyhouse.application.phoneverification.port.in.SmsVerificationConfirmCommand;
import com.tastyhouse.application.phoneverification.port.in.SmsVerificationConfirmUseCase;
import com.tastyhouse.application.phoneverification.port.in.SmsVerificationSendCommand;
import com.tastyhouse.application.phoneverification.port.in.SmsVerificationSendUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.ratelimit.RateLimit;
import com.tastyhouse.apicommon.ratelimit.RateLimitKeyType;
import com.tastyhouse.webapi.phoneverification.adapter.in.web.request.SmsVerificationConfirmRequest;
import com.tastyhouse.webapi.phoneverification.adapter.in.web.request.SmsVerificationSendRequest;
import com.tastyhouse.webapi.phoneverification.adapter.in.web.response.SmsVerificationTokenResponse;

@RestController
@RequestMapping("/api/sms-verifications")
@Tag(name = "SMS Verification", description = "SMS(휴대폰번호) 인증 API")
class SmsVerificationApiController {

    private final SmsVerificationSendUseCase smsVerificationSendUseCase;
    private final SmsVerificationConfirmUseCase smsVerificationConfirmUseCase;
    private final MemberJwtTokenProvider jwtTokenProvider;

    public SmsVerificationApiController(
        SmsVerificationSendUseCase smsVerificationSendUseCase,
        SmsVerificationConfirmUseCase smsVerificationConfirmUseCase,
        MemberJwtTokenProvider jwtTokenProvider
    ) {
        this.smsVerificationSendUseCase = smsVerificationSendUseCase;
        this.smsVerificationConfirmUseCase = smsVerificationConfirmUseCase;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Operation(
        summary = "인증번호 발송",
        description = "입력한 휴대폰번호로 6자리 인증번호를 SMS 발송합니다. 기존 미완료 인증은 자동 만료됩니다."
    )
    @RateLimit(limit = 5, windowSeconds = 86400, keyType = RateLimitKeyType.FIELD, keyField = "phoneNumber", keyPrefix = "rate_limit:sms_verification")
    @PostMapping("/v1/send")
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(
        @Valid @RequestBody SmsVerificationSendRequest request
    ) {
        SmsVerificationSendCommand command = request.toCommand();
        smsVerificationSendUseCase.sendVerificationCode(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(
        summary = "인증번호 확인",
        description = "발송된 인증번호를 검증합니다. 인증 성공 시 10분간 유효한 smsVerifyToken을 반환합니다. " +
                      "개인정보 수정(휴대폰번호 변경) 시 X-Sms-Verify-Token 헤더에 포함하여 사용합니다."
    )
    @PostMapping("/v1/confirm")
    public ResponseEntity<ApiResponse<SmsVerificationTokenResponse>> confirmVerificationCode(
        @Valid @RequestBody SmsVerificationConfirmRequest request
    ) {
        SmsVerificationConfirmCommand command = request.toCommand();
        String phoneNumber = smsVerificationConfirmUseCase.confirmVerificationCode(command);
        String smsVerifyToken = jwtTokenProvider.createSmsVerifyToken(phoneNumber);
        return ResponseEntity.ok(ApiResponse.success(
            SmsVerificationTokenResponse.from(smsVerifyToken)
        ));
    }
}
