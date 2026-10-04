package com.tastyhouse.infrastructure.redis;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.infrastructure.redis.token.RedisTokenStoreProperties;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RedisTokenStoreProperties.class)
class RedisModuleConfig {
}
