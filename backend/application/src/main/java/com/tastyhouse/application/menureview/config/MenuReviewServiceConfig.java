package com.tastyhouse.application.menureview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.menureview.port.out.write.MenuReviewPersistencePort;
import com.tastyhouse.application.menureview.service.MenuReviewLifecycleService;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class MenuReviewServiceConfig {

    @Bean
    public MenuReviewLifecycleService menuReviewLifecycleService(
        MenuReviewPersistencePort menuReviewPersistencePort,
        DomainEventPublisher domainEventPublisher
    ) {
        return new MenuReviewLifecycleService(menuReviewPersistencePort, domainEventPublisher);
    }
}
