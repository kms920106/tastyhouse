package com.tastyhouse.domain.policy.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.policy.event.PolicyActivatedEvent;
import com.tastyhouse.domain.policy.model.PolicyDocument;
import com.tastyhouse.domain.policy.repository.PolicyDocumentRepository;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

public class PolicyActivationService {
    private final PolicyDocumentRepository policyDocumentRepository;
    private final DomainEventPublisher domainEventPublisher;

    public PolicyActivationService(
        PolicyDocumentRepository policyDocumentRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        this.policyDocumentRepository = policyDocumentRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    public void activate(PolicyDocument newPolicy) {
        policyDocumentRepository.findCurrentEntityByType(newPolicy.getType())
            .ifPresent(current -> {
                current.deactivate();
                policyDocumentRepository.save(current);
            });

        newPolicy.activate();
        policyDocumentRepository.save(newPolicy);

        domainEventPublisher.publish(new PolicyActivatedEvent(
            newPolicy.getPolicyDocumentId(),
            newPolicy.getType(),
            newPolicy.getVersion(),
            LocalDateTime.now()
        ));
    }
}
