package com.tastyhouse.application.policy.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.policy.port.in.PolicyActivateCommand;
import com.tastyhouse.application.policy.port.in.PolicyCommandUseCase;
import com.tastyhouse.application.policy.port.in.PolicyCreateCommand;
import com.tastyhouse.application.policy.port.in.PolicyUpdateCommand;
import com.tastyhouse.domain.policy.model.PolicyDocument;
import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.domain.policy.repository.PolicyDocumentRepository;
import com.tastyhouse.domain.policy.service.PolicyActivationService;
import com.tastyhouse.domain.policy.vo.PolicyDocumentId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

@Service
@AdminApp
@Transactional
public class PolicyCommandService implements PolicyCommandUseCase {

    private final PolicyDocumentRepository policyDocumentRepository;
    private final PolicyActivationService policyActivationService;

    public PolicyCommandService(PolicyDocumentRepository policyDocumentRepository, PolicyActivationService policyActivationService) {
        this.policyDocumentRepository = policyDocumentRepository;
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
        PolicyDocument saved = policyDocumentRepository.save(policyDocument);
        return saved.getPolicyDocumentId().value();
    }

    @Override
    public void updatePolicy(PolicyUpdateCommand command) {
        PolicyDocumentId policyDocumentId = PolicyDocumentId.of(command.policyDocumentId());
        PolicyDocument policyDocument = findPolicyDocumentOrThrow(policyDocumentId);

        policyDocument.update(command.title(), command.content(), command.mandatory(), command.effectiveDate(), command.updatedBy());
        policyDocumentRepository.save(policyDocument);
    }

    @Override
    public void activateCurrentPolicy(PolicyActivateCommand command) {
        PolicyDocumentId policyDocumentId = PolicyDocumentId.of(command.policyDocumentId());
        PolicyDocument policyDocument = findPolicyDocumentOrThrow(policyDocumentId);

        policyActivationService.activate(policyDocument);
    }

    private PolicyDocument findPolicyDocumentOrThrow(PolicyDocumentId policyDocumentId) {
        return policyDocumentRepository.findById(policyDocumentId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POLICY_NOT_FOUND));
    }
}
