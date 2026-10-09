package com.tastyhouse.infrastructure.jpa.partnership.persistence;

import com.tastyhouse.domain.partnership.model.PartnershipRequest;
import com.tastyhouse.domain.partnership.model.PartnershipStatus;

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
            entity.getStatus() == null ? null : PartnershipStatus.valueOf(entity.getStatus()),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static PartnershipRequestJpaEntity toEntity(PartnershipRequest partnershipRequest) {
        return PartnershipRequestJpaEntity.create(
            partnershipRequest.getBusinessName(),
            partnershipRequest.getAddress(),
            partnershipRequest.getAddressDetail(),
            partnershipRequest.getContactName(),
            partnershipRequest.getContactPhone(),
            partnershipRequest.getConsultationRequestedAt(),
            partnershipRequest.getStatus() == null ? null : partnershipRequest.getStatus().name(),
            partnershipRequest.isDeleted()
        );
    }

    static void applyChanges(PartnershipRequestJpaEntity entity, PartnershipRequest partnershipRequest) {
        entity.applyChanges(
            partnershipRequest.getStatus() == null ? null : partnershipRequest.getStatus().name(),
            partnershipRequest.isDeleted()
        );
    }
}
