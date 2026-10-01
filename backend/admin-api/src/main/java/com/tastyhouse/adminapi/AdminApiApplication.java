package com.tastyhouse.adminapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import com.tastyhouse.application.AdminApplicationConfig;
import com.tastyhouse.adminapi.config.AdminSeedProperties;

@SpringBootApplication
@Import(AdminApplicationConfig.class)
@EnableConfigurationProperties(AdminSeedProperties.class)
public class AdminApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApiApplication.class, args);
    }
}
