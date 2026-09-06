package com.tastyhouse.application.partnership.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.partnership.model.PartnershipStatus;

public record PartnershipRequestDetailResult(
    Long id,
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
}
