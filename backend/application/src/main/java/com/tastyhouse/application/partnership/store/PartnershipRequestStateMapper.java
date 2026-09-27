package com.tastyhouse.application.partnership.store;

import com.tastyhouse.application.partnership.port.out.write.PartnershipRequestState;
import com.tastyhouse.domain.partnership.model.PartnershipRequest;
import com.tastyhouse.domain.partnership.model.PartnershipStatus;

final class PartnershipRequestStateMapper {
    private PartnershipRequestStateMapper() {
    }

    static PartnershipRequest toDomain(PartnershipRequestState state) {
        return PartnershipRequest.reconstitute(
            state.id(),
            state.businessName(),
            state.address(),
            state.addressDetail(),
            state.contactName(),
            state.contactPhone(),
            state.consultationRequestedAt(),
            state.status() == null ? null : PartnershipStatus.valueOf(state.status()),
            state.deleted(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static PartnershipRequestState toState(PartnershipRequest partnershipRequest) {
        return new PartnershipRequestState(
            partnershipRequest.getId(),
            partnershipRequest.getBusinessName(),
            partnershipRequest.getAddress(),
            partnershipRequest.getAddressDetail(),
            partnershipRequest.getContactName(),
            partnershipRequest.getContactPhone(),
            partnershipRequest.getConsultationRequestedAt(),
            partnershipRequest.getStatus() == null ? null : partnershipRequest.getStatus().name(),
            partnershipRequest.isDeleted(),
            partnershipRequest.getCreatedAt(),
            partnershipRequest.getUpdatedAt()
        );
    }
}
