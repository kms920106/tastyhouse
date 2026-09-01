package com.tastyhouse.apicommon.ratelimit;

import java.time.Duration;

/**
 * Rate limit 카운터 계약 — 표현 계층이 소유하고 인프라가 구현한다.
 *
 * <p>{@link RateLimitAspect}가 요청당 1회 호출한다. 카운터를 어디에 저장하는지(Redis·인메모리 등)는
 * 이 계약의 관심사가 아니며, 구현체는 {@code infrastructure:redis}의
 * {@code RedisRateLimitCounter}가 제공한다(어댑터 → 계약 방향).
 *
 * <p>이 포트를 표현 계층에 둔 이유는 챕터 02 재배치의 핵심이다 — 이전에는 api-common-module이
 * {@code RateLimitException} 처리를 위해 {@code infrastructure:redis}를 의존했고, 그 인프라 모듈이
 * {@code HttpServletRequest}로 클라이언트 IP를 해석하느라 서블릿 스택까지 끌어왔다. 웹 관심사를
 * 표현으로 올리고 카운터만 인프라에 남기면서 의존 방향이 바로잡혔다.
 */
public interface RateLimitCounterPort {

    /**
     * Fixed Window 방식으로 요청 횟수를 검증한다.
     *
     * @param key      카운터 키 (IP, 전화번호, 이메일 등)
     * @param limit    윈도우 기간 내 허용 최대 요청 수
     * @param duration 윈도우 기간
     * @return 제한 초과 여부 (true = 초과)
     */
    boolean isLimitExceeded(String key, int limit, Duration duration);
}
