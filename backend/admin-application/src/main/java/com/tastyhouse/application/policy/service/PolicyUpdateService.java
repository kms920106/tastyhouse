package com.tastyhouse.application.policy.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.policy.model.PolicyDocument;
import com.tastyhouse.domain.policy.vo.PolicyDocumentId;
import com.tastyhouse.application.policy.port.in.PolicyUpdateCommand;
import com.tastyhouse.application.policy.port.in.PolicyUpdateUseCase;
import com.tastyhouse.application.policy.port.out.write.PolicyDocumentLoadPort;
import com.tastyhouse.application.policy.port.out.write.PolicyDocumentSavePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class PolicyUpdateService implements PolicyUpdateUseCase {

    private final PolicyDocumentLoadPort policyDocumentLoadPort;
    private final PolicyDocumentSavePort policyDocumentSavePort;

    public PolicyUpdateService(PolicyDocumentLoadPort policyDocumentLoadPort, PolicyDocumentSavePort policyDocumentSavePort) {
        this.policyDocumentLoadPort = policyDocumentLoadPort;
        this.policyDocumentSavePort = policyDocumentSavePort;
    }

    @Override
    public void updatePolicy(PolicyUpdateCommand command) {
        PolicyDocumentId policyDocumentId = PolicyDocumentId.of(command.policyDocumentId());
        PolicyDocument policyDocument = findPolicyDocumentOrThrow(policyDocumentId);

        policyDocument.update(command.title(), command.content(), command.mandatory(), command.effectiveDate(), command.updatedBy());
        policyDocumentSavePort.save(policyDocument);
    }

    private PolicyDocument findPolicyDocumentOrThrow(PolicyDocumentId policyDocumentId) {
        return policyDocumentLoadPort.findById(policyDocumentId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.POLICY_NOT_FOUND));
    }
}
