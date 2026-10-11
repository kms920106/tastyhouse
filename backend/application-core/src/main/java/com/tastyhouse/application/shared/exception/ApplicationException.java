package com.tastyhouse.application.shared.exception;

import com.tastyhouse.domain.exception.BusinessException;

public class ApplicationException extends BusinessException {

    public ApplicationException(ApplicationErrorCodeSpec errorCode) {
        super(errorCode);
    }

    public ApplicationException(ApplicationErrorCodeSpec errorCode, String message) {
        super(errorCode, message);
    }

    public ApplicationException(ApplicationErrorCodeSpec errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
