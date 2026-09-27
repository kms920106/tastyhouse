package com.tastyhouse.application.policy.store;

import com.tastyhouse.domain.policy.model.PolicyDocument;
import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.application.policy.port.out.write.PolicyDocumentState;

final class PolicyDocumentStateMapper {
    private PolicyDocumentStateMapper() {
    }

    static PolicyDocument toDomain(PolicyDocumentState state) {
        return PolicyDocument.reconstitute(
            state.id(),
            state.type() == null ? null : PolicyType.valueOf(state.type()),
            state.version(),
            state.title(),
            state.content(),
            state.current(),
            state.mandatory(),
            state.effectiveDate(),
            state.createdBy(),
            state.updatedBy(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static PolicyDocumentState toState(PolicyDocument policyDocument) {
        return new PolicyDocumentState(
            policyDocument.getId(),
            policyDocument.getType() == null ? null : policyDocument.getType().name(),
            policyDocument.getVersion(),
            policyDocument.getTitle(),
            policyDocument.getContent(),
            policyDocument.isCurrent(),
            policyDocument.isMandatory(),
            policyDocument.getEffectiveDate(),
            policyDocument.getCreatedBy(),
            policyDocument.getUpdatedBy(),
            policyDocument.getCreatedAt(),
            policyDocument.getUpdatedAt()
        );
    }
}
