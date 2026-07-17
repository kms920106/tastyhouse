package com.tastyhouse.core.domain.partnership.application.dto.result;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.partnership.domain.model.PartnershipRequest;
import com.tastyhouse.core.domain.partnership.domain.model.PartnershipStatus;
import com.tastyhouse.core.domain.partnership.domain.vo.PartnershipRequestId;

public record PartnershipRequestResult(
    PartnershipRequestId id,
    String businessName,
    String address,
    String addressDetail,
    String contactName,
    String contactPhone,
    PartnershipStatus status,
    LocalDateTime consultationRequestedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static PartnershipRequestResult from(PartnershipRequest partnershipRequest) {
        return new PartnershipRequestResult(
            partnershipRequest.getPartnershipRequestId(),
            partnershipRequest.getBusinessName(),
            partnershipRequest.getAddress(),
            partnershipRequest.getAddressDetail(),
            partnershipRequest.getContactName(),
            partnershipRequest.getContactPhone(),
            partnershipRequest.getStatus(),
            partnershipRequest.getConsultationRequestedAt(),
            partnershipRequest.getCreatedAt(),
            partnershipRequest.getUpdatedAt()
        );
    }
}
