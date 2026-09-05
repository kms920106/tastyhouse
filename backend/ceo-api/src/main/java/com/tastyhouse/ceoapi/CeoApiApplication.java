package com.tastyhouse.ceoapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import com.tastyhouse.apicommon.ApiCommonConfig;
import com.tastyhouse.ceoapi.config.CeoSeedProperties;
import com.tastyhouse.application.CeoApplicationConfig;
import com.tastyhouse.external.config.ExternalModuleConfig;
import com.tastyhouse.external.firebase.FirebaseModuleConfig;
import com.tastyhouse.infrastructure.InfrastructureModuleConfig;
import com.tastyhouse.infrastructure.redis.RedisModuleConfig;
import com.tastyhouse.logging.LoggingModuleConfig;
import com.tastyhouse.security.SecurityModuleConfig;

@SpringBootApplication
@Import({InfrastructureModuleConfig.class, RedisModuleConfig.class, ExternalModuleConfig.class,
         FirebaseModuleConfig.class, SecurityModuleConfig.class, LoggingModuleConfig.class,
         ApiCommonConfig.class, CeoApplicationConfig.class})
@EnableConfigurationProperties(CeoSeedProperties.class)
public class CeoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CeoApiApplication.class, args);
    }
}
