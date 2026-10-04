package com.tastyhouse.infrastructure.redis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static org.assertj.core.api.Assertions.assertThat;

class RedisModuleConfigTest {

    @Test
    @DisplayName("stringRedisTemplate은 Boot 기본값이 아니라 우리 RedisConfig의 것이 등록된다")
    void ourStringRedisTemplateWinsOverBootDefault() {
        new ApplicationContextRunner()
            .withUserConfiguration(RedisModuleConfig.class, RedisConfig.class)
            .withConfiguration(AutoConfigurations.of(RedisAutoConfiguration.class))
            .run(context -> {
                assertThat(context).hasSingleBean(StringRedisTemplate.class);
                StringRedisTemplate template = context.getBean(StringRedisTemplate.class);
                assertThat(template.getKeySerializer())
                    .as("""
                        RedisConfig가 키·값 serializer를 명시 지정한다. \
                        앱이 스캔하는 사용자 설정은 auto-configuration보다 먼저 처리되므로 우리 정의가 먼저 \
                        등록되고 Boot의 @ConditionalOnMissingBean이 물러난다 — RedisConfig에서 이 빈을 지우면 \
                        Boot 기본 템플릿이 조용히 들어와 기존 Redis 데이터와 직렬화가 어긋난다.""")
                    .isInstanceOf(StringRedisSerializer.class);
                assertThat(template.getValueSerializer()).isInstanceOf(StringRedisSerializer.class);
            });
    }
}
