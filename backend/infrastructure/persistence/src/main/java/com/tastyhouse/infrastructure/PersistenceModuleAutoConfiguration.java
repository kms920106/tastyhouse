package com.tastyhouse.infrastructure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@AutoConfiguration(before = JpaRepositoriesAutoConfiguration.class)
@ComponentScan(
    basePackages = "com.tastyhouse.infrastructure",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.tastyhouse\\.infrastructure\\.redis\\..*"
    )
)
public class PersistenceModuleAutoConfiguration {
}
