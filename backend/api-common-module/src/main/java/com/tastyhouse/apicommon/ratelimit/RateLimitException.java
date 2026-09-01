package com.tastyhouse.apicommon.ratelimit;

/**
 * 요청 횟수 제한 초과 예외.
 *
 * <p>rate limiting은 domain-module에 대응 개념이 없는 순수 보안 관심사이므로(모듈 경계 규칙),
 * 이 예외는 {@code com.tastyhouse.domain.exception.ErrorCode}에 결합하지 않는다. 과거에는 생성자가
 * {@code ErrorCode.RATE_LIMIT_EXCEEDED.getDefaultMessage()}로 메시지를 채웠으나, 실제 HTTP 응답은
 * 각 api 모듈의 {@code GlobalExceptionHandler}가 {@code ErrorCode.RATE_LIMIT_EXCEEDED}의
 * code·message로 직접 조립하고 이 예외의 메시지는 읽지 않는다. 따라서 결합을 끊어도
 * 응답 계약(429 + {@code RATE_LIMIT_EXCEEDED})은 그대로다.
 */
public class RateLimitException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "요청 횟수가 초과되었습니다. 잠시 후 다시 시도해주세요.";

    public RateLimitException() {
        super(DEFAULT_MESSAGE);
    }
}
