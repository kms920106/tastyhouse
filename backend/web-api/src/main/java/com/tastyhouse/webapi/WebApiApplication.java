package com.tastyhouse.webapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.tastyhouse.apicommon.file.ApiCommonFileConfig;
import com.tastyhouse.external.config.ExternalApiConfig;
import com.tastyhouse.external.oauth.ExternalOAuthConfig;
import com.tastyhouse.infrastructure.InfrastructureModuleConfig;
import com.tastyhouse.logging.LoggingModuleConfig;
import com.tastyhouse.security.SecurityModuleConfig;

@SpringBootApplication
@Import({InfrastructureModuleConfig.class, ExternalApiConfig.class, ExternalOAuthConfig.class,
         SecurityModuleConfig.class, LoggingModuleConfig.class, ApiCommonFileConfig.class})
public class WebApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApiApplication.class, args);
    }
}
