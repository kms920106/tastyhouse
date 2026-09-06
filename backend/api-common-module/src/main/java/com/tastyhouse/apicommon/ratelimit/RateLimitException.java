package com.tastyhouse.apicommon.ratelimit;

public class RateLimitException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "요청 횟수가 초과되었습니다. 잠시 후 다시 시도해주세요.";

    public RateLimitException() {
        super(DEFAULT_MESSAGE);
    }
}
