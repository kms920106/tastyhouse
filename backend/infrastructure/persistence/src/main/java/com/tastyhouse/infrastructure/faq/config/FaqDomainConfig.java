package com.tastyhouse.infrastructure.faq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.faq.repository.FaqCategoryRepository;
import com.tastyhouse.domain.faq.service.FaqCategoryDeletionPolicy;

@Configuration(proxyBeanMethods = false)
public class FaqDomainConfig {
    @Bean
    public FaqCategoryDeletionPolicy faqCategoryDeletionPolicy(FaqCategoryRepository faqCategoryRepository) {
        return new FaqCategoryDeletionPolicy(faqCategoryRepository);
    }
}
