package com.tastyhouse.ceoapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import com.tastyhouse.application.CeoApplicationConfig;
import com.tastyhouse.ceoapi.config.CeoSeedProperties;

@SpringBootApplication
@Import(CeoApplicationConfig.class)
@EnableConfigurationProperties(CeoSeedProperties.class)
public class CeoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CeoApiApplication.class, args);
    }
}
