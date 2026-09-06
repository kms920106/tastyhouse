package com.tastyhouse.infrastructure.redis.ratelimit;

import java.time.Duration;
import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import com.tastyhouse.apicommon.ratelimit.RateLimitCounterPort;

@Component
public class RedisRateLimitCounter implements RateLimitCounterPort {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisRateLimitCounter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private static final RedisScript<Long> RATE_LIMIT_SCRIPT = RedisScript.of(
        "local count = redis.call('INCR', KEYS[1]) " +
        "if count == 1 then redis.call('PEXPIRE', KEYS[1], ARGV[1]) end " +
        "return count",
        Long.class
    );

    @Override
    @SuppressWarnings("ConstantConditions")
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
