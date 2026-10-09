package com.tastyhouse.application.policy.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.policy.event.PolicyActivatedEvent;
import com.tastyhouse.domain.policy.model.PolicyDocument;
import com.tastyhouse.application.policy.port.out.write.PolicyDocumentLoadPort;
import com.tastyhouse.application.policy.port.out.write.PolicyDocumentSavePort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;

@Service
public class PolicyActivationService {

    private final PolicyDocumentLoadPort policyDocumentLoadPort;
    private final PolicyDocumentSavePort policyDocumentSavePort;
    private final DomainEventPublisher domainEventPublisher;

    public PolicyActivationService(
        PolicyDocumentLoadPort policyDocumentLoadPort,
        PolicyDocumentSavePort policyDocumentSavePort,
        DomainEventPublisher domainEventPublisher
    ) {
        this.policyDocumentLoadPort = policyDocumentLoadPort;
        this.policyDocumentSavePort = policyDocumentSavePort;
        this.domainEventPublisher = domainEventPublisher;
    }

    public void activate(PolicyDocument newPolicy) {
        policyDocumentLoadPort.findCurrentEntityByType(newPolicy.getType())
            .ifPresent(current -> {
                current.deactivate();
                policyDocumentSavePort.save(current);
            });

        newPolicy.activate();
        policyDocumentSavePort.save(newPolicy);

        domainEventPublisher.publish(new PolicyActivatedEvent(
            newPolicy.getPolicyDocumentId(),
            newPolicy.getType(),
            newPolicy.getVersion(),
            LocalDateTime.now()
        ));
    }
}
