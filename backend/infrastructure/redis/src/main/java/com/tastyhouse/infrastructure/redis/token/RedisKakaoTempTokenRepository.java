package com.tastyhouse.infrastructure.redis.token;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.tastyhouse.security.token.KakaoTempTokenRepository;

/**
 * {@link KakaoTempTokenRepository}의 Redis 구현 — security-core가 소유한 계약을 인프라가 구현한다.
 *
 * <p>Key: {@code "kakao_temp:{tempToken}"} → kakaoAccessToken (TTL: 10분)
 * 소셜 임시 토큰은 앱 간 공유되지 않으므로 키 접두사 설정을 받지 않는다.
 */
@Component
public class RedisKakaoTempTokenRepository implements KakaoTempTokenRepository {

    private static final String PREFIX = "kakao_temp:";
    private static final long TTL_MINUTES = 10;

    private final StringRedisTemplate redisTemplate;

    public RedisKakaoTempTokenRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(String kakaoTempToken, String kakaoAccessToken) {
        redisTemplate.opsForValue().set(
            PREFIX + kakaoTempToken,
            kakaoAccessToken,
            TTL_MINUTES,
            TimeUnit.MINUTES
        );
    }

    @Override
    public String findKakaoAccessToken(String kakaoTempToken) {
        return redisTemplate.opsForValue().get(PREFIX + kakaoTempToken);
    }

    @Override
    public void delete(String kakaoTempToken) {
        redisTemplate.delete(PREFIX + kakaoTempToken);
    }
}
