package com.tastyhouse.core.domain.policy.application;

import com.tastyhouse.core.domain.policy.application.dto.command.CreatePolicyCommand;
import com.tastyhouse.core.domain.policy.application.dto.command.UpdatePolicyCommand;
import com.tastyhouse.core.domain.policy.domain.event.PolicyActivatedEvent;
import com.tastyhouse.core.domain.policy.domain.model.PolicyDocument;
import com.tastyhouse.core.domain.policy.domain.repository.PolicyDocumentRepository;
import com.tastyhouse.core.domain.policy.domain.vo.PolicyDocumentId;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class PolicyCommandService {

    private final PolicyDocumentRepository policyDocumentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PolicyDocumentId createPolicy(CreatePolicyCommand command) {
        PolicyDocument policyDocument = PolicyDocument.of(
            command.type(),
            command.version(),
            command.title(),
            command.content(),
            command.mandatory(),
            command.effectiveDate(),
            command.createdBy()
        );
        PolicyDocument saved = policyDocumentRepository.save(policyDocument);
        return new PolicyDocumentId(saved.getId());
    }

    public void activatePolicy(PolicyDocumentId id) {
        PolicyDocument newPolicy = policyDocumentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POLICY_NOT_FOUND));

        policyDocumentRepository.findCurrentEntityByType(newPolicy.getType())
            .ifPresent(PolicyDocument::deactivate);

        newPolicy.activate();
        policyDocumentRepository.save(newPolicy);
        eventPublisher.publishEvent(new PolicyActivatedEvent(
            new PolicyDocumentId(newPolicy.getId()), newPolicy.getType(), newPolicy.getVersion(), LocalDateTime.now()
        ));
    }

    public void updatePolicy(PolicyDocumentId id, UpdatePolicyCommand command) {
        PolicyDocument policyDocument = policyDocumentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POLICY_NOT_FOUND));

        policyDocument.update(
            command.title(),
            command.content(),
            command.mandatory(),
            command.effectiveDate(),
            command.updatedBy()
        );
    }
}
