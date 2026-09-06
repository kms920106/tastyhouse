package com.tastyhouse.infrastructure.redis.token;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * refresh 토큰 키가 {@code {keyPrefix}rt:{username}}으로 조립되는지 고정값으로 단정한다.
 *
 * <p>접두사 조합이 어긋나도 컴파일·기동은 성공하고 <b>기존 세션만 조용히 무효화</b>되므로,
 * 이 단언이 그 유일한 자동 방어선이다. 여기 적힌 문자열은 운영 Redis에 실재하는 키 공간이며
 * 앱별 접두사({@code ""}/{@code "admin:"}/{@code "ceo:"})는 불변 계약이다.
 */
class RedisRefreshTokenRepositoryTest {

    private static final long TTL_MILLIS = 604_800_000L;

    @Test
    @DisplayName("keyPrefix가 비면 web-api 키 공간 rt:{username}을 쓴다")
    void usesBareRefreshKeyWhenPrefixIsEmpty() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mockValueOperations(redisTemplate);
        RedisRefreshTokenRepository repository = new RedisRefreshTokenRepository(
            redisTemplate, new RedisTokenStoreProperties(""));

        repository.save("member01", "refresh-token", TTL_MILLIS);

        verify(valueOperations).set("rt:member01", "refresh-token", TTL_MILLIS, TimeUnit.MILLISECONDS);
    }

    @Test
    @DisplayName("admin 접두사는 admin:rt:{username}으로 조립된다")
    void prependsAdminPrefix() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mockValueOperations(redisTemplate);
        RedisRefreshTokenRepository repository = new RedisRefreshTokenRepository(
            redisTemplate, new RedisTokenStoreProperties("admin:"));

        repository.save("admin01", "refresh-token", TTL_MILLIS);
        repository.delete("admin01");

        verify(valueOperations).set("admin:rt:admin01", "refresh-token", TTL_MILLIS, TimeUnit.MILLISECONDS);
        verify(redisTemplate).delete("admin:rt:admin01");
    }

    @Test
    @DisplayName("ceo 접두사는 ceo:rt:{username}으로 조립되고 조회도 같은 키를 읽는다")
    void prependsCeoPrefixOnRead() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mockValueOperations(redisTemplate);
        when(valueOperations.get("ceo:rt:ceo01")).thenReturn("stored-token");
        RedisRefreshTokenRepository repository = new RedisRefreshTokenRepository(
            redisTemplate, new RedisTokenStoreProperties("ceo:"));

        assertThat(repository.find("ceo01")).isEqualTo("stored-token");
        assertThat(repository.isInvalid("ceo01", "stored-token")).isFalse();
        assertThat(repository.isInvalid("ceo01", "other-token")).isTrue();
    }

    @SuppressWarnings("unchecked")
    private ValueOperations<String, String> mockValueOperations(StringRedisTemplate redisTemplate) {
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        return valueOperations;
    }
}
