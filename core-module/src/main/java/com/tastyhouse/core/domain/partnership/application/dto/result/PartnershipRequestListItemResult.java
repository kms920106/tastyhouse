package com.tastyhouse.core.domain.partnership.application.dto.result;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.partnership.domain.model.PartnershipStatus;

public record PartnershipRequestListItemResult(
    Long id,
    String businessName,
    String contactName,
    String contactPhone,
    PartnershipStatus status,
    LocalDateTime consultationRequestedAt,
    LocalDateTime createdAt
) {
    @QueryProjection
    public PartnershipRequestListItemResult {
    }
}
