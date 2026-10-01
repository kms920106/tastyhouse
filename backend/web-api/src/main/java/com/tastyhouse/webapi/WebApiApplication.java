package com.tastyhouse.webapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.tastyhouse.application.WebApplicationConfig;

@SpringBootApplication
@Import(WebApplicationConfig.class)
public class WebApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApiApplication.class, args);
    }
}
