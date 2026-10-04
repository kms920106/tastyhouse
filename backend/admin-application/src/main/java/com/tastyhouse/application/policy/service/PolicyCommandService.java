package com.tastyhouse.application.policy.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.policy.model.PolicyDocument;
import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.domain.policy.vo.PolicyDocumentId;
import com.tastyhouse.application.policy.port.in.PolicyActivateCommand;
import com.tastyhouse.application.policy.port.in.PolicyCommandUseCase;
import com.tastyhouse.application.policy.port.in.PolicyCreateCommand;
import com.tastyhouse.application.policy.port.in.PolicyUpdateCommand;
import com.tastyhouse.application.policy.port.out.write.PolicyDocumentPersistencePort;

@Service
@Transactional
public class PolicyCommandService implements PolicyCommandUseCase {

    private final PolicyDocumentPersistencePort policyDocumentPersistencePort;
    private final PolicyActivationService policyActivationService;

    public PolicyCommandService(PolicyDocumentPersistencePort policyDocumentPersistencePort, PolicyActivationService policyActivationService) {
        this.policyDocumentPersistencePort = policyDocumentPersistencePort;
        this.policyActivationService = policyActivationService;
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

    @Override
    public void updatePolicy(PolicyUpdateCommand command) {
        PolicyDocumentId policyDocumentId = PolicyDocumentId.of(command.policyDocumentId());
        PolicyDocument policyDocument = findPolicyDocumentOrThrow(policyDocumentId);

        policyDocument.update(command.title(), command.content(), command.mandatory(), command.effectiveDate(), command.updatedBy());
        policyDocumentPersistencePort.save(policyDocument);
    }

    @Override
    public void activateCurrentPolicy(PolicyActivateCommand command) {
        PolicyDocumentId policyDocumentId = PolicyDocumentId.of(command.policyDocumentId());
        PolicyDocument policyDocument = findPolicyDocumentOrThrow(policyDocumentId);

        policyActivationService.activate(policyDocument);
    }

    private PolicyDocument findPolicyDocumentOrThrow(PolicyDocumentId policyDocumentId) {
        return policyDocumentPersistencePort.findById(policyDocumentId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POLICY_NOT_FOUND));
    }
}
