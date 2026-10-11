package com.tastyhouse.application.partnership.port.out;

import java.time.LocalDateTime;

public record PartnershipSearchCondition(
    String businessName,
    String contactName,
    String contactPhone,
    String status,
    LocalDateTime startDate,
    LocalDateTime endDate
) {

    public static PartnershipSearchCondition of(
        String businessName,
        String contactName,
        String contactPhone,
        String status,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        return new PartnershipSearchCondition(businessName, contactName, contactPhone, status, startDate, endDate);
    }
}
