package com.tastyhouse.adminapi.exception;

import com.tastyhouse.core.exception.ErrorCode;

public class RateLimitException extends RuntimeException {

    public RateLimitException() {
        super(ErrorCode.RATE_LIMIT_EXCEEDED.getDefaultMessage());
    }
}
