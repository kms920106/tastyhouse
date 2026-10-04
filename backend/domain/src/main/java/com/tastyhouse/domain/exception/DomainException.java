package com.tastyhouse.domain.exception;

public class DomainException extends BusinessException {

    public DomainException(DomainErrorCode errorCode) {
        super(errorCode);
    }

    public DomainException(DomainErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public DomainException(DomainErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
