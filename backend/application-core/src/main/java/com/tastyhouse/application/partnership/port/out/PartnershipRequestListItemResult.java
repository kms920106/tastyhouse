package com.tastyhouse.application.partnership.port.out;

import java.time.LocalDateTime;

public record PartnershipRequestListItemResult(
    Long id,
    String businessName,
    String contactName,
    String contactPhone,
    String status,
    LocalDateTime consultationRequestedAt,
    LocalDateTime createdAt
) {
}
