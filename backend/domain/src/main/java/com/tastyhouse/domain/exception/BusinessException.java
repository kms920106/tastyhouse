package com.tastyhouse.domain.exception;

public abstract class BusinessException extends RuntimeException {

    private final ErrorCodeSpec errorCode;

    protected BusinessException(ErrorCodeSpec errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    protected BusinessException(ErrorCodeSpec errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected BusinessException(ErrorCodeSpec errorCode, Throwable cause) {
        super(errorCode.getDefaultMessage(), cause);
        this.errorCode = errorCode;
    }

    public ErrorCodeSpec getErrorCode() {
        return this.errorCode;
    }
}
