package com.tastyhouse.infrastructure.partnership.persistence;

import com.tastyhouse.application.partnership.port.out.write.PartnershipRequestState;

final class PartnershipRequestMapper {
    private PartnershipRequestMapper() {
    }

    static PartnershipRequestState toState(PartnershipRequestJpaEntity entity) {
        return new PartnershipRequestState(
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

    static PartnershipRequestJpaEntity toEntity(PartnershipRequestState state) {
        return PartnershipRequestJpaEntity.create(
            state.businessName(),
            state.address(),
            state.addressDetail(),
            state.contactName(),
            state.contactPhone(),
            state.consultationRequestedAt(),
            state.status(),
            state.deleted()
        );
    }

    static void applyChanges(PartnershipRequestJpaEntity entity, PartnershipRequestState state) {
        entity.applyChanges(
            state.status(),
            state.deleted()
        );
    }
}
