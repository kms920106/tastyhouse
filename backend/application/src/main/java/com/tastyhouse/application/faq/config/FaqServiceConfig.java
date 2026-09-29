package com.tastyhouse.application.faq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.faq.port.out.write.FaqCategoryPersistencePort;
import com.tastyhouse.application.faq.service.FaqCategoryDeletionPolicy;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class FaqServiceConfig {
    @Bean
    public FaqCategoryDeletionPolicy faqCategoryDeletionPolicy(FaqCategoryPersistencePort faqCategoryPersistencePort) {
        return new FaqCategoryDeletionPolicy(faqCategoryPersistencePort);
    }
}
