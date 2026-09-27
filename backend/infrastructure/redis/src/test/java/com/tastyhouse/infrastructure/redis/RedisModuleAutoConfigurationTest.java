package com.tastyhouse.infrastructure.redis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static org.assertj.core.api.Assertions.assertThat;

class RedisModuleAutoConfigurationTest {

    @Test
    @DisplayName("stringRedisTemplate은 Boot 기본값이 아니라 우리 RedisConfig의 것이 등록된다")
    void ourStringRedisTemplateWinsOverBootDefault() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                RedisModuleAutoConfiguration.class,
                RedisAutoConfiguration.class))
            .run(context -> {
                assertThat(context).hasSingleBean(StringRedisTemplate.class);
                StringRedisTemplate template = context.getBean(StringRedisTemplate.class);
                assertThat(template.getKeySerializer())
                    .as("""
                        RedisConfig가 키·값 serializer를 명시 지정한다. \
                        @AutoConfiguration(before = RedisAutoConfiguration.class)로 우리 정의가 먼저 \
                        등록돼야 Boot의 @ConditionalOnMissingBean이 물러난다 — before를 빼거나 after로 \
                        바꾸면 이름이 겹쳐 기동이 실패하고(loud), RedisConfig에서 이 빈을 지우면 \
                        Boot 기본 템플릿이 조용히 들어와 기존 Redis 데이터와 직렬화가 어긋난다.""")
                    .isInstanceOf(StringRedisSerializer.class);
                assertThat(template.getValueSerializer()).isInstanceOf(StringRedisSerializer.class);
            });
    }
}
