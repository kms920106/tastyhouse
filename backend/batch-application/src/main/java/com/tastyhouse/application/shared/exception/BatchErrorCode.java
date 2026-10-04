package com.tastyhouse.application.shared.exception;

public enum BatchErrorCode implements ApplicationErrorCodeSpec {

    ADMIN_DONG_BOUNDARY_FETCH_FAILED(502, "ADMIN_DONG_BOUNDARY_FETCH_FAILED", "행정동 경계 데이터를 가져오지 못했습니다.");

    private final int httpStatusCode;
    private final String code;
    private final String defaultMessage;

    BatchErrorCode(int httpStatusCode, String code, String defaultMessage) {
        this.httpStatusCode = httpStatusCode;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    @Override
    public int getHttpStatusCode() {
        return this.httpStatusCode;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDefaultMessage() {
        return this.defaultMessage;
    }
}
