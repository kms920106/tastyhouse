package com.tastyhouse.apicommon;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tastyhouse.apicommon.exception.GlobalExceptionHandler;
import com.tastyhouse.apicommon.ratelimit.ApiCommonRateLimitAutoConfiguration;
import com.tastyhouse.apicommon.ratelimit.RateLimitAspect;
import com.tastyhouse.infrastructure.redis.RedisModuleAutoConfiguration;
import com.tastyhouse.security.ratelimit.RateLimitCounterPort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ApiCommonAutoConfigurationTest {

    private static final AutoConfigurations AUTO_CONFIGURATIONS = AutoConfigurations.of(
        ApiCommonModuleAutoConfiguration.class,
        ApiCommonRateLimitAutoConfiguration.class
    );

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

    @Nested
    @DisplayName("서블릿 웹 앱")
    class ServletWebApplication {

        private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AUTO_CONFIGURATIONS);

        @Test
        @DisplayName("자체 @RestControllerAdvice 빈이 있으면 공용 핸들러는 등록되지 않는다 (web-api)")
        void sharedHandlerBacksOffWhenAppHasOwnAdvice() {
            runner.withUserConfiguration(OwnAdviceConfig.class)
                .run(context -> {
                    assertThat(context).doesNotHaveBean("sharedGlobalExceptionHandler");
                    assertThat(context).hasSingleBean(OwnGlobalExceptionHandler.class);
                });
        }

        @Test
        @DisplayName("자체 advice가 없으면 공용 핸들러가 등록된다 (admin-api·ceo-api)")
        void sharedHandlerRegisteredWhenNoAdvicePresent() {
            runner.run(context -> {
                assertThat(context).hasBean("sharedGlobalExceptionHandler");
                assertThat(context).hasSingleBean(GlobalExceptionHandler.class);
            });
        }

        @Test
        @DisplayName("RateLimitCounterPort 빈이 없으면 aspect가 등록되지 않는다")
        void rateLimitAspectBacksOffWithoutCounter() {
            runner.run(context -> assertThat(context).doesNotHaveBean(RateLimitAspect.class));
        }

        @Test
        @DisplayName("RateLimitCounterPort 빈이 있으면 aspect가 등록된다 (web·admin·ceo)")
        void rateLimitAspectRegisteredWithCounter() {
            runner.withUserConfiguration(CounterConfig.class)
                .run(context -> assertThat(context).hasSingleBean(RateLimitAspect.class));
        }
    }

    @Nested
    @DisplayName("비-서블릿 앱 (batch-module)")
    class NonServletApplication {

        private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AUTO_CONFIGURATIONS);

        @Test
        @DisplayName("전이 의존으로 클래스패스에 있어도 두 빈 모두 등록되지 않는다")
        void bothBeansAbsentInNonServletContext() {
            runner.withUserConfiguration(CounterConfig.class)
                .run(context -> {
                    assertThat(context).doesNotHaveBean("sharedGlobalExceptionHandler");
                    assertThat(context).doesNotHaveBean(RateLimitAspect.class);
                });
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class OwnAdviceConfig {
        @Bean
        OwnGlobalExceptionHandler ownGlobalExceptionHandler() {
            return new OwnGlobalExceptionHandler();
        }
    }

    @RestControllerAdvice
    static class OwnGlobalExceptionHandler {
        @ExceptionHandler(RuntimeException.class)
        ProblemDetail handle(RuntimeException e) {
            return ProblemDetail.forStatus(500);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class CounterConfig {
        @Bean
        RateLimitCounterPort rateLimitCounterPort() {
            return (key, limit, window) -> false;
        }
    }
}
