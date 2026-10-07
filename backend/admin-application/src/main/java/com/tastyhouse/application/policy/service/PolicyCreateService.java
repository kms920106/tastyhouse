package com.tastyhouse.application.policy.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.policy.model.PolicyDocument;
import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.application.policy.port.in.PolicyCreateCommand;
import com.tastyhouse.application.policy.port.in.PolicyCreateUseCase;
import com.tastyhouse.application.policy.port.out.write.PolicyDocumentPersistencePort;

@Service
@Transactional
class PolicyCreateService implements PolicyCreateUseCase {

    private final PolicyDocumentPersistencePort policyDocumentPersistencePort;

    public PolicyCreateService(PolicyDocumentPersistencePort policyDocumentPersistencePort) {
        this.policyDocumentPersistencePort = policyDocumentPersistencePort;
    }

    @Override
    public Long createPolicy(PolicyCreateCommand command) {
        PolicyDocument policyDocument = PolicyDocument.of(
            PolicyType.from(command.type()),
            command.version(),
            command.title(),
            command.content(),
            command.mandatory(),
            command.effectiveDate(),
            command.createdBy()
        );
        PolicyDocument saved = policyDocumentPersistencePort.save(policyDocument);
        return saved.getPolicyDocumentId().value();
    }
}
