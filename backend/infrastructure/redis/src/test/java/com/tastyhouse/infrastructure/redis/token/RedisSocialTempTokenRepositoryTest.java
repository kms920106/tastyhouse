package com.tastyhouse.infrastructure.redis.token;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.tastyhouse.security.token.SocialTempTokenProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisSocialTempTokenRepositoryTest {

    @ParameterizedTest
    @CsvSource({
        "KAKAO, kakao_temp:temp-token",
        "NAVER, naver_temp:temp-token",
        "FACEBOOK, facebook_temp:temp-token",
        "APPLE, apple_temp:temp-token"
    })
    @DisplayName("provider별 키는 기존 {provider}_temp:{tempToken} 문자열 그대로이고 TTL은 10분이다")
    void savesUnderLegacyKeyWithTenMinuteTtl(SocialTempTokenProvider provider, String expectedKey) {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mockValueOperations(redisTemplate);
        RedisSocialTempTokenRepository repository = new RedisSocialTempTokenRepository(redisTemplate);

        repository.save(provider, "temp-token", "credential-value");

        verify(valueOperations).set(expectedKey, "credential-value", 10L, TimeUnit.MINUTES);
    }

    @ParameterizedTest
    @CsvSource({
        "KAKAO, kakao_temp:temp-token",
        "NAVER, naver_temp:temp-token",
        "FACEBOOK, facebook_temp:temp-token",
        "APPLE, apple_temp:temp-token"
    })
    @DisplayName("조회와 삭제는 저장과 같은 키를 본다")
    void findsAndDeletesSameKey(SocialTempTokenProvider provider, String expectedKey) {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mockValueOperations(redisTemplate);
        when(valueOperations.get(expectedKey)).thenReturn("credential-value");
        RedisSocialTempTokenRepository repository = new RedisSocialTempTokenRepository(redisTemplate);

        assertThat(repository.findCredential(provider, "temp-token")).isEqualTo("credential-value");
        repository.delete(provider, "temp-token");

        verify(redisTemplate).delete(expectedKey);
    }

    @Test
    @DisplayName("다른 provider의 임시토큰은 같은 tempToken이어도 키가 겹치지 않는다")
    void providersDoNotShareKeys() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mockValueOperations(redisTemplate);
        when(valueOperations.get("kakao_temp:temp-token")).thenReturn("kakao-credential");
        RedisSocialTempTokenRepository repository = new RedisSocialTempTokenRepository(redisTemplate);

        assertThat(repository.findCredential(SocialTempTokenProvider.KAKAO, "temp-token")).isEqualTo("kakao-credential");
        assertThat(repository.findCredential(SocialTempTokenProvider.NAVER, "temp-token")).isNull();
    }

    @SuppressWarnings("unchecked")
    private ValueOperations<String, String> mockValueOperations(StringRedisTemplate redisTemplate) {
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        return valueOperations;
    }
}
