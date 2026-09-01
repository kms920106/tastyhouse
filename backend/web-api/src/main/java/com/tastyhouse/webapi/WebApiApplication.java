package com.tastyhouse.webapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.tastyhouse.apicommon.file.ApiCommonFileConfig;
import com.tastyhouse.apicommon.ratelimit.ApiCommonRateLimitConfig;
import com.tastyhouse.external.config.ExternalApiConfig;
import com.tastyhouse.external.oauth.ExternalOAuthConfig;
import com.tastyhouse.infrastructure.InfrastructureModuleConfig;
import com.tastyhouse.infrastructure.redis.RedisModuleConfig;
import com.tastyhouse.logging.LoggingModuleConfig;
import com.tastyhouse.security.SecurityModuleConfig;
import com.tastyhouse.webapplication.WebApplicationConfig;

@SpringBootApplication
@Import({InfrastructureModuleConfig.class, RedisModuleConfig.class, ExternalApiConfig.class,
         ExternalOAuthConfig.class, SecurityModuleConfig.class, LoggingModuleConfig.class,
         ApiCommonFileConfig.class, ApiCommonRateLimitConfig.class, WebApplicationConfig.class})
public class WebApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApiApplication.class, args);
    }
}
