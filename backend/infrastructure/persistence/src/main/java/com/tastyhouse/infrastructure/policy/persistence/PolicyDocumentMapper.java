package com.tastyhouse.infrastructure.policy.persistence;

import com.tastyhouse.application.policy.port.out.write.PolicyDocumentState;

final class PolicyDocumentMapper {
    private PolicyDocumentMapper() {
    }

    static PolicyDocumentState toState(PolicyDocumentJpaEntity entity) {
        return new PolicyDocumentState(
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

    static PolicyDocumentJpaEntity toEntity(PolicyDocumentState state) {
        return PolicyDocumentJpaEntity.create(
            state.type(),
            state.version(),
            state.title(),
            state.content(),
            state.current(),
            state.mandatory(),
            state.effectiveDate(),
            state.createdBy(),
            state.updatedBy()
        );
    }

    static void applyChanges(PolicyDocumentJpaEntity entity, PolicyDocumentState state) {
        entity.applyChanges(
            state.title(),
            state.content(),
            state.mandatory(),
            state.effectiveDate(),
            state.updatedBy(),
            state.current()
        );
    }
}
