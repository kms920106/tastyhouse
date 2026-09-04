package com.tastyhouse.external.exception;

import com.tastyhouse.domain.exception.ErrorCodeSpec;

public enum ExternalApiErrorCode implements ErrorCodeSpec {

    // SMS
    SMS_SEND_NO_RESPONSE(502, "SMS_SEND_NO_RESPONSE", "SMS 발송 응답이 없습니다."),
    SMS_SEND_FAILED(502, "SMS_SEND_FAILED", "SMS 발송에 실패했습니다."),
    SMS_SEND_API_ERROR(502, "SMS_SEND_API_ERROR", "SMS 발송 중 API 오류가 발생했습니다."),

    // Mail
    MAIL_SEND_FAILED(502, "MAIL_SEND_FAILED", "이메일 발송에 실패했습니다."),

    // Region
    ADMIN_DONG_BOUNDARY_FETCH_FAILED(502, "ADMIN_DONG_BOUNDARY_FETCH_FAILED", "행정동 경계 데이터를 가져오지 못했습니다.");

    private final int httpStatusCode;
    private final String code;
    private final String defaultMessage;

    ExternalApiErrorCode(int httpStatusCode, String code, String defaultMessage) {
        this.httpStatusCode = httpStatusCode;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public int getHttpStatusCode() {
        return this.httpStatusCode;
    }

    public String getCode() {
        return this.code;
    }

    public String getDefaultMessage() {
        return this.defaultMessage;
    }
}
