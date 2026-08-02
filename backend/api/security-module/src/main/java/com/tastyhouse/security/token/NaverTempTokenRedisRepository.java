package com.tastyhouse.security.token;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * 네이버 임시 토큰 Redis 저장소
 *
 * <p>Key: "naver_temp:{tempToken}" → naverAccessToken (TTL: 10분)
 * - NEEDS_SIGN_UP / NEEDS_LINKING 응답 시 발급
 * - 회원가입(/signup/naver) 또는 계정 연동(/link/naver) 완료 시 삭제 (1회용)
 */
@Repository
public class NaverTempTokenRedisRepository {

    private static final String PREFIX = "naver_temp:";
    private static final long TTL_MINUTES = 10;

    private final StringRedisTemplate redisTemplate;

    public NaverTempTokenRedisRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String naverTempToken, String naverAccessToken) {
        redisTemplate.opsForValue().set(
            PREFIX + naverTempToken,
            naverAccessToken,
            TTL_MINUTES,
            TimeUnit.MINUTES
        );
    }

    public String findNaverAccessToken(String naverTempToken) {
        return redisTemplate.opsForValue().get(PREFIX + naverTempToken);
    }

    public void delete(String naverTempToken) {
        redisTemplate.delete(PREFIX + naverTempToken);
    }
}
