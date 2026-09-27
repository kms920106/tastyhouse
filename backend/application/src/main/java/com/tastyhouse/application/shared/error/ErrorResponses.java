package com.tastyhouse.application.shared.error;

import java.util.Optional;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCodeSpec;

public final class ErrorResponses {

    private ErrorResponses() {
    }

    public static Optional<ErrorDescriptor> resolve(Throwable e) {
        if (!(e instanceof BusinessException businessException)) {
            return Optional.empty();
        }
        ErrorCodeSpec errorCode = businessException.getErrorCode();
        return Optional.of(new ErrorDescriptor(
            errorCode.getHttpStatusCode(),
            errorCode.getCode(),
            businessException.getMessage()
        ));
    }
}
