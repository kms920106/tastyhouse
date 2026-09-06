package com.tastyhouse.webapi.mail.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.mail.port.in.MailVerificationCommandUseCase;
import com.tastyhouse.application.mail.port.in.MailVerificationConfirmCommand;
import com.tastyhouse.application.mail.port.in.MailVerificationSendCommand;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.ratelimit.RateLimit;
import com.tastyhouse.apicommon.ratelimit.RateLimitKeyType;
import com.tastyhouse.application.auth.token.MemberJwtTokenProvider;
import com.tastyhouse.webapi.mail.adapter.in.web.request.MailVerificationConfirmRequest;
import com.tastyhouse.webapi.mail.adapter.in.web.request.MailVerificationSendRequest;
import com.tastyhouse.webapi.mail.adapter.in.web.response.MailVerificationTokenResponse;

@RestController
@RequestMapping("/api/mail-verifications")
@Tag(name = "Mail Verification", description = "메일(이메일 주소) 인증 API")
public class MailVerificationApiController {
    private final MailVerificationCommandUseCase mailVerificationCommandUseCase;
    private final MemberJwtTokenProvider jwtTokenProvider;

    public MailVerificationApiController(
        MailVerificationCommandUseCase mailVerificationCommandUseCase,
        MemberJwtTokenProvider jwtTokenProvider
    ) {
        this.mailVerificationCommandUseCase = mailVerificationCommandUseCase;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Operation(
        summary = "인증번호 발송",
        description = "입력한 이메일로 6자리 인증번호를 발송합니다. 기존 미완료 인증은 자동 만료됩니다."
    )

    @RateLimit(limit = 5, windowSeconds = 86400, keyType = RateLimitKeyType.FIELD, keyField = "email", keyPrefix = "rate_limit:email_verification")
    @PostMapping("/v1/send")
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(
        @Valid @RequestBody MailVerificationSendRequest request
    ) {
        MailVerificationSendCommand command = request.toCommand();
        mailVerificationCommandUseCase.sendVerificationCode(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(
        summary = "인증번호 확인",
        description = "발송된 인증번호를 검증합니다. 인증 성공 시 10분간 유효한 mailVerifyToken을 반환합니다. " +
                      "회원가입 시 mailVerifyToken을 포함하여 사용합니다."
    )
    @PostMapping("/v1/confirm")
    public ResponseEntity<ApiResponse<MailVerificationTokenResponse>> confirmVerificationCode(
        @Valid @RequestBody MailVerificationConfirmRequest request
    ) {
        MailVerificationConfirmCommand command = request.toCommand();
        String email = mailVerificationCommandUseCase.confirmVerificationCode(command);
        String mailVerifyToken = jwtTokenProvider.createMailVerifyToken(email);
        return ResponseEntity.ok(ApiResponse.success(MailVerificationTokenResponse.from(mailVerifyToken)));
    }
}
