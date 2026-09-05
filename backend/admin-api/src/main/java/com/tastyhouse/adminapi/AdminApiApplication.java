package com.tastyhouse.adminapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import com.tastyhouse.adminapi.config.AdminSeedProperties;
import com.tastyhouse.application.AdminApplicationConfig;

@SpringBootApplication
@Import(AdminApplicationConfig.class)
@EnableConfigurationProperties(AdminSeedProperties.class)
public class AdminApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApiApplication.class, args);
    }
}
