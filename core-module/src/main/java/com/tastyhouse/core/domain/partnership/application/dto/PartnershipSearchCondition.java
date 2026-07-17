package com.tastyhouse.core.domain.partnership.application.dto;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.partnership.domain.model.PartnershipStatus;

public record PartnershipSearchCondition(
    String businessName,
    String contactName,
    String contactPhone,
    PartnershipStatus status,
    LocalDateTime startDate,
    LocalDateTime endDate
) {

    public static PartnershipSearchCondition of(
        String businessName,
        String contactName,
        String contactPhone,
        PartnershipStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        return new PartnershipSearchCondition(businessName, contactName, contactPhone, status, startDate, endDate);
    }
}
