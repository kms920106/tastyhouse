package com.tastyhouse.application.policy.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.policy.model.PolicyDocument;
import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.application.policy.port.in.PolicyCreateCommand;
import com.tastyhouse.application.policy.port.in.PolicyCreateUseCase;
import com.tastyhouse.application.policy.port.out.write.PolicyDocumentSavePort;

@Service
@Transactional
class PolicyCreateService implements PolicyCreateUseCase {

    private final PolicyDocumentSavePort policyDocumentSavePort;

    public PolicyCreateService(PolicyDocumentSavePort policyDocumentSavePort) {
        this.policyDocumentSavePort = policyDocumentSavePort;
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
        PolicyDocument saved = policyDocumentSavePort.save(policyDocument);
        return saved.getPolicyDocumentId().value();
    }
}
