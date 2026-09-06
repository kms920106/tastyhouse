package com.tastyhouse.infrastructure.policy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.policy.repository.PolicyDocumentRepository;
import com.tastyhouse.domain.policy.service.PolicyActivationService;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

@Configuration(proxyBeanMethods = false)
public class PolicyDomainConfig {
    @Bean
    public PolicyActivationService policyActivationService(
        PolicyDocumentRepository policyDocumentRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new PolicyActivationService(policyDocumentRepository, domainEventPublisher);
    }
}
