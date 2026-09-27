package com.tastyhouse.application.shared.config;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.event.SpringDomainEventPublisher;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class SharedEventConfig {

    @Bean
    public DomainEventPublisher domainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new SpringDomainEventPublisher(applicationEventPublisher);
    }
}
