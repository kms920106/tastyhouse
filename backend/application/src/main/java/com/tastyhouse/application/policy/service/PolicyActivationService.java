package com.tastyhouse.application.policy.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.policy.event.PolicyActivatedEvent;
import com.tastyhouse.domain.policy.model.PolicyDocument;
import com.tastyhouse.application.policy.port.out.write.PolicyDocumentPersistencePort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;

public class PolicyActivationService {
    private final PolicyDocumentPersistencePort policyDocumentPersistencePort;
    private final DomainEventPublisher domainEventPublisher;

    public PolicyActivationService(
        PolicyDocumentPersistencePort policyDocumentPersistencePort,
        DomainEventPublisher domainEventPublisher
    ) {
        this.policyDocumentPersistencePort = policyDocumentPersistencePort;
        this.domainEventPublisher = domainEventPublisher;
    }

    public void activate(PolicyDocument newPolicy) {
        policyDocumentPersistencePort.findCurrentEntityByType(newPolicy.getType())
            .ifPresent(current -> {
                current.deactivate();
                policyDocumentPersistencePort.save(current);
            });

        newPolicy.activate();
        policyDocumentPersistencePort.save(newPolicy);

        domainEventPublisher.publish(new PolicyActivatedEvent(
            newPolicy.getPolicyDocumentId(),
            newPolicy.getType(),
            newPolicy.getVersion(),
            LocalDateTime.now()
        ));
    }
}
