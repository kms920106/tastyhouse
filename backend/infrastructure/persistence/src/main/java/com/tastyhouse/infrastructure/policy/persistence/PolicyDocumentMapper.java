package com.tastyhouse.infrastructure.policy.persistence;

import com.tastyhouse.domain.policy.model.PolicyDocument;
import com.tastyhouse.domain.policy.model.PolicyType;

final class PolicyDocumentMapper {

    private PolicyDocumentMapper() {
    }

    static PolicyDocument toDomain(PolicyDocumentJpaEntity entity) {
        return PolicyDocument.reconstitute(
            entity.getId(),
            entity.getType() == null ? null : PolicyType.valueOf(entity.getType()),
            entity.getVersion(),
            entity.getTitle(),
            entity.getContent(),
            entity.isCurrent(),
            entity.isMandatory(),
            entity.getEffectiveDate(),
            entity.getCreatedBy(),
            entity.getUpdatedBy(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static PolicyDocumentJpaEntity toEntity(PolicyDocument policyDocument) {
        return PolicyDocumentJpaEntity.create(
            policyDocument.getType() == null ? null : policyDocument.getType().name(),
            policyDocument.getVersion(),
            policyDocument.getTitle(),
            policyDocument.getContent(),
            policyDocument.isCurrent(),
            policyDocument.isMandatory(),
            policyDocument.getEffectiveDate(),
            policyDocument.getCreatedBy(),
            policyDocument.getUpdatedBy()
        );
    }

    static void applyChanges(PolicyDocumentJpaEntity entity, PolicyDocument policyDocument) {
        entity.applyChanges(
            policyDocument.getTitle(),
            policyDocument.getContent(),
            policyDocument.isMandatory(),
            policyDocument.getEffectiveDate(),
            policyDocument.getUpdatedBy(),
            policyDocument.isCurrent()
        );
    }
}
