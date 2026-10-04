package com.tastyhouse.apicommon.ratelimit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.security.ratelimit.RateLimitCounterPort;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class ApiCommonRateLimitConfig {

    @Bean
    public RateLimitAspect rateLimitAspect(RateLimitCounterPort rateLimitCounter) {
        return new RateLimitAspect(rateLimitCounter);
    }
}
