package com.tastyhouse.application.menureview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.menureview.port.out.write.MenuReviewStatePort;
import com.tastyhouse.application.menureview.service.MenuReviewLifecycleService;
import com.tastyhouse.application.menureview.store.MenuReviewRepository;
import com.tastyhouse.application.menureview.store.MenuReviewStore;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class MenuReviewServiceConfig {
    @Bean
    public MenuReviewRepository menuReviewRepository(MenuReviewStatePort menuReviewStatePort) {
        return new MenuReviewStore(menuReviewStatePort);
    }

    @Bean
    public MenuReviewLifecycleService menuReviewLifecycleService(
        MenuReviewRepository menuReviewRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new MenuReviewLifecycleService(menuReviewRepository, domainEventPublisher);
    }
}
