package com.tastyhouse.application.policy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.policy.port.out.write.PolicyDocumentStatePort;
import com.tastyhouse.application.policy.store.PolicyDocumentRepository;
import com.tastyhouse.application.policy.store.PolicyDocumentStore;
import com.tastyhouse.application.policy.service.PolicyActivationService;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class PolicyServiceConfig {
    @Bean
    public PolicyDocumentRepository policyDocumentRepository(PolicyDocumentStatePort policyDocumentStatePort) {
        return new PolicyDocumentStore(policyDocumentStatePort);
    }

    @Bean
    public PolicyActivationService policyActivationService(
        PolicyDocumentRepository policyDocumentRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new PolicyActivationService(policyDocumentRepository, domainEventPublisher);
    }
}
