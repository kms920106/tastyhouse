package com.tastyhouse.external.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExternalApiErrorCode {

    // SMS
    SMS_SEND_NO_RESPONSE(502, "SMS_SEND_NO_RESPONSE", "SMS 발송 응답이 없습니다."),
    SMS_SEND_FAILED(502, "SMS_SEND_FAILED", "SMS 발송에 실패했습니다."),
    SMS_SEND_API_ERROR(502, "SMS_SEND_API_ERROR", "SMS 발송 중 API 오류가 발생했습니다."),

    // Email
    EMAIL_SEND_FAILED(502, "EMAIL_SEND_FAILED", "이메일 발송에 실패했습니다.");

    private final int httpStatusCode;
    private final String code;
    private final String defaultMessage;
}
