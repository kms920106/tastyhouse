package com.tastyhouse.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.tastyhouse.application.BatchApplicationConfig;
import com.tastyhouse.external.config.ExternalModuleConfig;
import com.tastyhouse.external.crawling.CrawlingModuleConfig;
import com.tastyhouse.external.firebase.FirebaseModuleConfig;
import com.tastyhouse.infrastructure.InfrastructureModuleConfig;
import com.tastyhouse.logging.LoggingModuleConfig;

@EnableScheduling
@SpringBootApplication
@Import({InfrastructureModuleConfig.class, ExternalModuleConfig.class, FirebaseModuleConfig.class,
         CrawlingModuleConfig.class, LoggingModuleConfig.class, BatchApplicationConfig.class})
public class BatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }
}
