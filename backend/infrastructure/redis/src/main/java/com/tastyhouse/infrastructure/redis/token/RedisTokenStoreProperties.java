package com.tastyhouse.infrastructure.redis.token;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "security.token-store")
public record RedisTokenStoreProperties(@DefaultValue("") String keyPrefix) {
}
