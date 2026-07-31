package com.tastyhouse.security.ratelimit;

import com.tastyhouse.domain.exception.ErrorCode;

public class RateLimitException extends RuntimeException {

    public RateLimitException() {
        super(ErrorCode.RATE_LIMIT_EXCEEDED.getDefaultMessage());
    }
}
