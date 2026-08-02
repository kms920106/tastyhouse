package com.tastyhouse.security.token;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * 페이스북 임시 토큰 Redis 저장소
 *
 * <p>Key: "facebook_temp:{tempToken}" → facebookAccessToken (TTL: 10분)
 * - NEEDS_SIGN_UP / NEEDS_LINKING 응답 시 발급
 * - 회원가입(/signup/facebook) 또는 계정 연동(/link/facebook) 완료 시 삭제 (1회용)
 */
@Repository
public class FacebookTempTokenRedisRepository {

    private static final String PREFIX = "facebook_temp:";
    private static final long TTL_MINUTES = 10;

    private final StringRedisTemplate redisTemplate;

    public FacebookTempTokenRedisRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String facebookTempToken, String facebookAccessToken) {
        redisTemplate.opsForValue().set(
            PREFIX + facebookTempToken,
            facebookAccessToken,
            TTL_MINUTES,
            TimeUnit.MINUTES
        );
    }

    public String findFacebookAccessToken(String facebookTempToken) {
        return redisTemplate.opsForValue().get(PREFIX + facebookTempToken);
    }

    public void delete(String facebookTempToken) {
        redisTemplate.delete(PREFIX + facebookTempToken);
    }
}
