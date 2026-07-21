package com.tastyhouse.webapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.tastyhouse.webapi", "com.tastyhouse.core", "com.tastyhouse.infrastructure", "com.tastyhouse.external", "com.tastyhouse.logging"})
public class WebApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApiApplication.class, args);
    }
}
