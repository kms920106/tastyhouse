package com.tastyhouse.external.exception;

import com.tastyhouse.domain.exception.BusinessException;

public class ExternalApiException extends BusinessException {

    public ExternalApiException(ExternalApiErrorCode errorCode) {
        super(errorCode);
    }

    public ExternalApiException(ExternalApiErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ExternalApiException(ExternalApiErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
