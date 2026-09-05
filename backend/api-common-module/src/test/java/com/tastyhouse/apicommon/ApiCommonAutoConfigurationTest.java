package com.tastyhouse.apicommon;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import com.tastyhouse.apicommon.ratelimit.RateLimitCounterPort;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * api-common-module auto-configuration의 조건 검증.
 *
 * <p>이 모듈의 두 빈은 앱마다 켜지고 꺼지는 것이 다르다 — 공용 예외 핸들러는 자체 advice가 없는
 * 앱(admin·ceo)에서만, rate limit aspect는 카운터 구현이 있는 서블릿 앱에서만 등록된다.
 * 그 조건이 의도대로 동작하는지를 컨텍스트를 실제로 띄우지 않고 검증한다.
 *
 * <p>이 테스트는 단위 수준 근거이고, 실제 회귀 방지는 4개 앱 기동 후의 조건 리포트·
 * 로그인 rate limit 실측이 담당한다(챕터 02 문서 §9).
 */
class ApiCommonAutoConfigurationTest {

    private static final AutoConfigurations AUTO_CONFIGURATIONS = AutoConfigurations.of(
        ApiCommonModuleAutoConfiguration.class,
        ApiCommonRateLimitAutoConfiguration.class
    );

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

        /** ApplicationContextRunner 기본값이 비-웹 컨텍스트다 — batch의 {@code web-application-type: none}에 해당. */
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

    /** web-api의 {@code com.tastyhouse.webapi.exception.GlobalExceptionHandler} 역할. */
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
