package com.tastyhouse.application.partnership.port.out.write;

import java.time.LocalDateTime;

public record PartnershipRequestState(
    Long id,
    String businessName,
    String address,
    String addressDetail,
    String contactName,
    String contactPhone,
    LocalDateTime consultationRequestedAt,
    String status,
    boolean deleted,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
