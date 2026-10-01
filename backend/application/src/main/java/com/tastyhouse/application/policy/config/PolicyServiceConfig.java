package com.tastyhouse.application.policy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.policy.port.out.write.PolicyDocumentPersistencePort;
import com.tastyhouse.application.policy.service.PolicyActivationService;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class PolicyServiceConfig {

    @Bean
    public PolicyActivationService policyActivationService(
        PolicyDocumentPersistencePort policyDocumentPersistencePort,
        DomainEventPublisher domainEventPublisher
    ) {
        return new PolicyActivationService(policyDocumentPersistencePort, domainEventPublisher);
    }
}
