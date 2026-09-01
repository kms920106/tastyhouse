package com.tastyhouse.infrastructure.redis.ratelimit;

import java.time.Duration;
import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import com.tastyhouse.apicommon.ratelimit.RateLimitCounterPort;

/**
 * {@link RateLimitCounterPort}의 Redis 구현 — 표현 계층이 소유한 계약을 인프라가 구현한다.
 *
 * <p>과거 {@code RateLimiterService}였고, 챕터 02에서 포트 구현체로 전환하며 개명했다.
 * Lua 스크립트와 키 취급은 그대로이므로 <b>기존 Redis 카운터 키와 호환</b>된다
 * (키 접두사는 호출부의 {@code @RateLimit(keyPrefix=...)}가 결정한다).
 */
@Component
public class RedisRateLimitCounter implements RateLimitCounterPort {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisRateLimitCounter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * INCR + PEXPIRE를 단일 Lua 스크립트로 원자적으로 실행합니다.
     * - 첫 번째 요청 시 TTL을 설정하므로 서버 중단 시에도 키가 영구 잔류하지 않습니다.
     * - Fixed Window 방식: 윈도우 시작 시점에 카운터가 초기화됩니다.
     */
    private static final RedisScript<Long> RATE_LIMIT_SCRIPT = RedisScript.of(
        "local count = redis.call('INCR', KEYS[1]) " +
        "if count == 1 then redis.call('PEXPIRE', KEYS[1], ARGV[1]) end " +
        "return count",
        Long.class
    );

    @Override
    @SuppressWarnings("ConstantConditions") // execute()는 @Nullable 반환 — Redis 계층에서 null 가능성이 있어 방어 코드 유지
    public boolean isLimitExceeded(String key, int limit, Duration duration) {
        Long count = stringRedisTemplate.execute(
            RATE_LIMIT_SCRIPT,
            List.of(key),
            String.valueOf(duration.toMillis())
        );
        long currentCount = count == null ? 0L : count;
        return currentCount > limit;
    }
}
