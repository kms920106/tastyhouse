package com.tastyhouse.infrastructure.redis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.tastyhouse.apicommon.ratelimit.ApiCommonRateLimitAutoConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class RedisModuleAutoConfigurationTest {

    @Test
    @DisplayName("ApiCommonRateLimitAutoConfiguration의 afterName이 실재하는 클래스를 가리킨다")
    void rateLimitAutoConfigurationAfterNameResolvesToRealClass() {
        String[] afterNames = ApiCommonRateLimitAutoConfiguration.class
            .getAnnotation(AutoConfiguration.class)
            .afterName();

        assertThat(afterNames)
            .as("rate limit aspect의 @ConditionalOnBean 가시성이 이 순서 선언에 달려 있다")
            .containsExactly(RedisModuleAutoConfiguration.class.getName());

        for (String name : afterNames) {
            assertThatCode(() -> Class.forName(name))
                .as("""
                    afterName은 문자열이라 오타·리네임을 컴파일러가 잡지 못한다. \
                    틀리면 Boot가 조용히 무시하고, @ConditionalOnBean이 카운터 빈을 보지 못해 \
                    RateLimitAspect가 사라진다 — 컴파일·빌드·기동 전부 성공하므로 \
                    admin·ceo 로그인 rate limit이 소리 없이 없어진다. 이 단언이 그 유일한 자동 방어선이다.""")
                .doesNotThrowAnyException();
        }
    }

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
