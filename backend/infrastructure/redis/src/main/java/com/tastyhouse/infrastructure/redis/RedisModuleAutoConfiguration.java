package com.tastyhouse.infrastructure.redis;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.tastyhouse.infrastructure.redis.token.RedisTokenStoreProperties;

@AutoConfiguration(before = RedisAutoConfiguration.class)
@ComponentScan("com.tastyhouse.infrastructure.redis")
@EnableConfigurationProperties(RedisTokenStoreProperties.class)
public class RedisModuleAutoConfiguration {
}
