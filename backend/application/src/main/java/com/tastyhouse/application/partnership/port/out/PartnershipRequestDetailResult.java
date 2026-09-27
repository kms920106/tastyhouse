package com.tastyhouse.application.partnership.port.out;

import java.time.LocalDateTime;

public record PartnershipRequestDetailResult(
    Long id,
    String businessName,
    String address,
    String addressDetail,
    String contactName,
    String contactPhone,
    String status,
    LocalDateTime consultationRequestedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
