package com.tastyhouse.apicommon.ratelimit;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(afterName = "com.tastyhouse.infrastructure.redis.RedisModuleAutoConfiguration")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ApiCommonRateLimitAutoConfiguration {

    @Bean
    @ConditionalOnBean(RateLimitCounterPort.class)
    public RateLimitAspect rateLimitAspect(RateLimitCounterPort rateLimitCounter) {
        return new RateLimitAspect(rateLimitCounter);
    }
}
