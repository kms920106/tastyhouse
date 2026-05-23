package com.tastyhouse.core.domain.partnership.application.dto.result;

import com.tastyhouse.core.domain.partnership.domain.model.PartnershipRequest;

import java.time.LocalDateTime;

public record PartnershipRequestResult(
    Long id,
    String businessName,
    String address,
    String addressDetail,
    String contactName,
    String contactPhone,
    LocalDateTime consultationRequestedAt,
    LocalDateTime createdAt
) {
    public static PartnershipRequestResult from(PartnershipRequest partnershipRequest) {
        return new PartnershipRequestResult(
            partnershipRequest.getId(),
            partnershipRequest.getBusinessName(),
            partnershipRequest.getAddress(),
            partnershipRequest.getAddressDetail(),
            partnershipRequest.getContactName(),
            partnershipRequest.getContactPhone(),
            partnershipRequest.getConsultationRequestedAt(),
            partnershipRequest.getCreatedAt()
        );
    }
}
