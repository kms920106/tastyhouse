package com.tastyhouse.external.crawling;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.tastyhouse.external.crawling.bbq.BbqProperties;
import com.tastyhouse.external.region.AdminDongBoundaryProperties;

@AutoConfiguration
@ComponentScan(basePackages = {
    "com.tastyhouse.external.crawling",
    "com.tastyhouse.external.region"
})
@EnableConfigurationProperties({
    BbqProperties.class,
    AdminDongBoundaryProperties.class
})
public class CrawlingModuleAutoConfiguration {
}
