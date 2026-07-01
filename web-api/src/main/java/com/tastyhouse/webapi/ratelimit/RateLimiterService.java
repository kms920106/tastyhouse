package com.tastyhouse.webapi.ratelimit;

import java.time.Duration;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final StringRedisTemplate stringRedisTemplate;

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

    /**
     * Fixed Window 방식으로 요청 횟수를 검증합니다.
     *
     * @param key      Redis 키 (IP, 전화번호, 이메일 등)
     * @param limit    윈도우 기간 내 허용 최대 요청 수
     * @param duration 윈도우 기간
     * @return 제한 초과 여부 (true = 초과)
     */
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
