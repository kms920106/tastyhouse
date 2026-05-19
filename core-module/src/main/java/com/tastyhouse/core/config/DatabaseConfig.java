package com.tastyhouse.core.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = {"com.tastyhouse.core.repository", "com.tastyhouse.core.domain"})
@EntityScan(basePackages = {"com.tastyhouse.core.entity", "com.tastyhouse.core.domain"})
@EnableJpaAuditing
@EnableTransactionManagement
public class DatabaseConfig {
}