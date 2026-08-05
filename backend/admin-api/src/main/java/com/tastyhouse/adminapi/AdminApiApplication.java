package com.tastyhouse.adminapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import com.tastyhouse.adminapi.config.AdminSeedProperties;
import com.tastyhouse.apicommon.ApiCommonConfig;
import com.tastyhouse.external.config.ExternalApiConfig;
import com.tastyhouse.infrastructure.InfrastructureModuleConfig;
import com.tastyhouse.logging.LoggingModuleConfig;
import com.tastyhouse.security.SecurityModuleConfig;

@SpringBootApplication
@Import({InfrastructureModuleConfig.class, ExternalApiConfig.class,
         SecurityModuleConfig.class, LoggingModuleConfig.class, ApiCommonConfig.class})
@EnableConfigurationProperties(AdminSeedProperties.class)
public class AdminApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApiApplication.class, args);
    }
}
