package com.tastyhouse.infrastructure.policy.persistence;

import com.tastyhouse.domain.policy.model.PolicyDocument;

final class PolicyDocumentMapper {
    private PolicyDocumentMapper() {
    }

    static PolicyDocument toDomain(PolicyDocumentJpaEntity entity) {
        return PolicyDocument.reconstitute(
            entity.getId(),
            entity.getType(),
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

    static PolicyDocumentJpaEntity toEntity(PolicyDocument domain) {
        return PolicyDocumentJpaEntity.create(
            domain.getType(),
            domain.getVersion(),
            domain.getTitle(),
            domain.getContent(),
            domain.isCurrent(),
            domain.isMandatory(),
            domain.getEffectiveDate(),
            domain.getCreatedBy(),
            domain.getUpdatedBy()
        );
    }

    static void applyChanges(PolicyDocumentJpaEntity entity, PolicyDocument domain) {
        entity.applyChanges(
            domain.getTitle(),
            domain.getContent(),
            domain.isMandatory(),
            domain.getEffectiveDate(),
            domain.getUpdatedBy(),
            domain.isCurrent()
        );
    }
}
