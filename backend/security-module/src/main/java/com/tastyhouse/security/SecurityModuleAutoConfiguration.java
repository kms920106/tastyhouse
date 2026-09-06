package com.tastyhouse.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.tastyhouse.security.jwt.JwtAuthenticationFilter;
import com.tastyhouse.security.jwt.JwtProperties;
import com.tastyhouse.security.jwt.JwtTokenProvider;
import com.tastyhouse.security.token.BlacklistRepository;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ComponentScan("com.tastyhouse.security")
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityModuleAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(JwtAuthenticationFilter.class)
    public JwtAuthenticationFilter jwtAuthenticationFilter(
        JwtTokenProvider jwtTokenProvider,
        BlacklistRepository blacklistRepository,
        ObjectMapper objectMapper
    ) {
        return new JwtAuthenticationFilter(jwtTokenProvider, blacklistRepository, objectMapper);
    }
}
