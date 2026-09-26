package com.tastyhouse.restclient.config;

import java.time.Duration;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class RestClientConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

    @Bean
    public RestClientCustomizer restClientTimeoutCustomizer() {
        return builder -> builder.requestFactory(HttpRequestFactories.withTimeouts(CONNECT_TIMEOUT, READ_TIMEOUT));
    }
}
