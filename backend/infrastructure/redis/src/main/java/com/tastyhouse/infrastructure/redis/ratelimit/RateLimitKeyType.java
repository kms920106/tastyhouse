package com.tastyhouse.infrastructure.redis.ratelimit;

public enum RateLimitKeyType {

    /** 클라이언트 IP 기반 */
    IP,

    /** 요청 본문의 특정 필드 기반 - @RateLimit의 keyField로 필드명을 명시해야 합니다. */
    FIELD
}
