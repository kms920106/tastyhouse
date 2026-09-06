package com.tastyhouse.infrastructure.menureview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.menureview.repository.MenuReviewRepository;
import com.tastyhouse.domain.menureview.service.MenuReviewLifecycleService;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

@Configuration(proxyBeanMethods = false)
public class MenuReviewDomainConfig {
    @Bean
    public MenuReviewLifecycleService menuReviewLifecycleService(
        MenuReviewRepository menuReviewRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new MenuReviewLifecycleService(menuReviewRepository, domainEventPublisher);
    }
}
