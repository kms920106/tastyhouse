package com.tastyhouse.domain.exception;

public class BusinessException extends RuntimeException {

    private final ErrorCodeSpec errorCode;

    public BusinessException(ErrorCodeSpec errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCodeSpec errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCodeSpec errorCode, Throwable cause) {
        super(errorCode.getDefaultMessage(), cause);
        this.errorCode = errorCode;
    }

    public ErrorCodeSpec getErrorCode() {
        return this.errorCode;
    }
}
