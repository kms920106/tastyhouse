package com.tastyhouse.application.policy.service;

import java.time.LocalDateTime;

import com.tastyhouse.application.policy.port.out.write.PolicyDocumentRepository;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.domain.policy.event.PolicyActivatedEvent;
import com.tastyhouse.domain.policy.model.PolicyDocument;

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
