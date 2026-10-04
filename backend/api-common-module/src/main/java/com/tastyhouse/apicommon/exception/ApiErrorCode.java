package com.tastyhouse.apicommon.exception;

public enum ApiErrorCode {

    ACCESS_DENIED(403, "ACCESS_DENIED", "접근 권한이 없습니다."),
    RATE_LIMIT_EXCEEDED(429, "RATE_LIMIT_EXCEEDED", "요청 횟수가 초과되었습니다. 잠시 후 다시 시도해주세요."),
    AUTH_REQUIRED(401, "AUTH_REQUIRED", "인증이 필요합니다.");

    private final int httpStatusCode;
    private final String code;
    private final String defaultMessage;

    ApiErrorCode(int httpStatusCode, String code, String defaultMessage) {
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
