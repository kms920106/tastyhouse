package com.tastyhouse.infrastructure.persistence;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration(before = JpaRepositoriesAutoConfiguration.class)
@ComponentScan("com.tastyhouse.infrastructure.persistence")
public class PersistenceModuleAutoConfiguration {
}
