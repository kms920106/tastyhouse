package com.tastyhouse.application.partnership.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.partnership.model.PartnershipStatus;

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
