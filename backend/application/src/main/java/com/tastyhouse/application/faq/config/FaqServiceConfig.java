package com.tastyhouse.application.faq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.faq.port.out.write.FaqCategoryStatePort;
import com.tastyhouse.application.faq.port.out.write.FaqStatePort;
import com.tastyhouse.application.faq.service.FaqCategoryDeletionPolicy;
import com.tastyhouse.application.faq.store.FaqCategoryRepository;
import com.tastyhouse.application.faq.store.FaqCategoryStore;
import com.tastyhouse.application.faq.store.FaqRepository;
import com.tastyhouse.application.faq.store.FaqStore;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class FaqServiceConfig {
    @Bean
    public FaqRepository faqRepository(FaqStatePort faqStatePort) {
        return new FaqStore(faqStatePort);
    }

    @Bean
    public FaqCategoryRepository faqCategoryRepository(FaqCategoryStatePort faqCategoryStatePort) {
        return new FaqCategoryStore(faqCategoryStatePort);
    }

    @Bean
    public FaqCategoryDeletionPolicy faqCategoryDeletionPolicy(FaqCategoryRepository faqCategoryRepository) {
        return new FaqCategoryDeletionPolicy(faqCategoryRepository);
    }
}
