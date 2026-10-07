package com.tastyhouse.application.policy.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.policy.model.PolicyDocument;
import com.tastyhouse.domain.policy.vo.PolicyDocumentId;
import com.tastyhouse.application.policy.port.in.PolicyActivateCommand;
import com.tastyhouse.application.policy.port.in.PolicyActivateUseCase;
import com.tastyhouse.application.policy.port.out.write.PolicyDocumentPersistencePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class PolicyActivateService implements PolicyActivateUseCase {

    private final PolicyDocumentPersistencePort policyDocumentPersistencePort;
    private final PolicyActivationService policyActivationService;

    public PolicyActivateService(PolicyDocumentPersistencePort policyDocumentPersistencePort, PolicyActivationService policyActivationService) {
        this.policyDocumentPersistencePort = policyDocumentPersistencePort;
        this.policyActivationService = policyActivationService;
    }

    @Override
    public void activateCurrentPolicy(PolicyActivateCommand command) {
        PolicyDocumentId policyDocumentId = PolicyDocumentId.of(command.policyDocumentId());
        PolicyDocument policyDocument = findPolicyDocumentOrThrow(policyDocumentId);

        policyActivationService.activate(policyDocument);
    }

    private PolicyDocument findPolicyDocumentOrThrow(PolicyDocumentId policyDocumentId) {
        return policyDocumentPersistencePort.findById(policyDocumentId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.POLICY_NOT_FOUND));
    }
}
