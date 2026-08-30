package com.tastyhouse.infrastructure.redis.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Redis 기반 Rate Limiting 어노테이션.
 *
 * <p>적용된 메서드에 요청 제한을 설정합니다.
 * {@code keyType}에 따라 IP 또는 요청 본문의 특정 필드를 기준으로 제한합니다.
 *
 * <pre>
 * 예시:
 * {@literal @}RateLimit(limit = 10, windowSeconds = 60, keyType = RateLimitKeyType.IP)
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** 윈도우 기간 내 허용 최대 요청 수 */
    int limit();

    /** 윈도우 기간 (초) */
    long windowSeconds();

    /** Rate Limit 키 추출 방식 */
    RateLimitKeyType keyType();

    /**
     * keyType이 {@link RateLimitKeyType#FIELD}일 때 추출할 요청 객체의 필드명.
     * 레코드 컴포넌트 접근자(e.g. phoneNumber()) 또는 getter(e.g. getPhoneNumber()) 순으로 시도합니다.
     */
    String keyField() default "";

    /** Redis 키 접두사 (중복 방지용 네임스페이스) */
    String keyPrefix() default "rate_limit";
}
