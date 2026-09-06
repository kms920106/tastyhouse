package com.tastyhouse.infrastructure.redis.token;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 블랙리스트 키가 {@code {keyPrefix}bl:{accessToken}}으로 조립되는지 고정값으로 단정한다.
 *
 * <p>{@link RedisRefreshTokenRepositoryTest}와 같은 이유로 존재한다 — 접두사가 어긋나면
 * 로그아웃한 토큰이 블랙리스트에 걸리지 않아 <b>예외 없이 계속 통과</b>한다.
 */
class RedisBlacklistRepositoryTest {

    @Test
    @DisplayName("keyPrefix가 비면 web-api 키 공간 bl:{accessToken}을 쓴다")
    void usesBareBlacklistKeyWhenPrefixIsEmpty() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mockValueOperations(redisTemplate);
        RedisBlacklistRepository repository = new RedisBlacklistRepository(
            redisTemplate, new RedisTokenStoreProperties(""));

        repository.add("access-token", System.currentTimeMillis() + 60_000L);

        verify(valueOperations).set(eq("bl:access-token"), eq("logout"), anyLong(), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("ceo 접두사는 ceo:bl:{accessToken}으로 조립되고 조회도 같은 키를 본다")
    void prependsCeoPrefix() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mockValueOperations(redisTemplate);
        when(redisTemplate.hasKey("ceo:bl:access-token")).thenReturn(true);
        RedisBlacklistRepository repository = new RedisBlacklistRepository(
            redisTemplate, new RedisTokenStoreProperties("ceo:"));

        repository.add("access-token", System.currentTimeMillis() + 60_000L);

        verify(valueOperations).set(eq("ceo:bl:access-token"), eq("logout"), anyLong(), eq(TimeUnit.MILLISECONDS));
        assertThat(repository.contains("access-token")).isTrue();
    }

    @Test
    @DisplayName("이미 만료된 토큰은 저장하지 않는다")
    void skipsAlreadyExpiredToken() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mockValueOperations(redisTemplate);
        RedisBlacklistRepository repository = new RedisBlacklistRepository(
            redisTemplate, new RedisTokenStoreProperties("admin:"));

        repository.add("access-token", System.currentTimeMillis() - 1L);

        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @SuppressWarnings("unchecked")
    private ValueOperations<String, String> mockValueOperations(StringRedisTemplate redisTemplate) {
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        return valueOperations;
    }
}
