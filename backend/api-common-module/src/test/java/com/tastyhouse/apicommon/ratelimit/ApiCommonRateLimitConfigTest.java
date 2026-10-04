package com.tastyhouse.apicommon.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.security.ratelimit.RateLimitCounterPort;

import static org.assertj.core.api.Assertions.assertThat;

class ApiCommonRateLimitConfigTest {

    @Test
    @DisplayName("서블릿 웹 앱에서는 RateLimitCounterPort로 aspect가 등록된다 (web·admin·ceo)")
    void rateLimitAspectRegisteredInServletContext() {
        new WebApplicationContextRunner()
            .withUserConfiguration(CounterConfig.class, ApiCommonRateLimitConfig.class)
            .run(context -> assertThat(context).hasSingleBean(RateLimitAspect.class));
    }

    @Test
    @DisplayName("비-서블릿 앱에서는 aspect가 등록되지 않는다 (batch-module)")
    void rateLimitAspectAbsentInNonServletContext() {
        new ApplicationContextRunner()
            .withUserConfiguration(CounterConfig.class, ApiCommonRateLimitConfig.class)
            .run(context -> assertThat(context).doesNotHaveBean(RateLimitAspect.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class CounterConfig {

        @Bean
        RateLimitCounterPort rateLimitCounterPort() {
            return (key, limit, window) -> false;
        }
    }
}
