package com.tastyhouse.infrastructure.persistence;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackageClasses = InfrastructurePersistenceConfig.class)
@EntityScan(basePackageClasses = InfrastructurePersistenceConfig.class)
@EnableJpaAuditing
@EnableTransactionManagement
class InfrastructurePersistenceConfig {
}
