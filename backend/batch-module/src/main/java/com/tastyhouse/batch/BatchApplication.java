package com.tastyhouse.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.tastyhouse.application.BatchApplicationConfig;

@EnableScheduling
@SpringBootApplication
@Import(BatchApplicationConfig.class)
public class BatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }
}
