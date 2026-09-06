package com.tastyhouse.infrastructure.partnership.persistence;

import com.tastyhouse.domain.partnership.model.PartnershipRequest;

final class PartnershipRequestMapper {
    private PartnershipRequestMapper() {
    }

    static PartnershipRequest toDomain(PartnershipRequestJpaEntity entity) {
        return PartnershipRequest.reconstitute(
            entity.getId(),
            entity.getBusinessName(),
            entity.getAddress(),
            entity.getAddressDetail(),
            entity.getContactName(),
            entity.getContactPhone(),
            entity.getConsultationRequestedAt(),
            entity.getStatus(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static PartnershipRequestJpaEntity toEntity(PartnershipRequest domain) {
        return PartnershipRequestJpaEntity.create(
            domain.getBusinessName(),
            domain.getAddress(),
            domain.getAddressDetail(),
            domain.getContactName(),
            domain.getContactPhone(),
            domain.getConsultationRequestedAt(),
            domain.getStatus(),
            domain.isDeleted()
        );
    }

    static void applyChanges(PartnershipRequestJpaEntity entity, PartnershipRequest domain) {
        entity.applyChanges(
            domain.getStatus(),
            domain.isDeleted()
        );
    }
}
