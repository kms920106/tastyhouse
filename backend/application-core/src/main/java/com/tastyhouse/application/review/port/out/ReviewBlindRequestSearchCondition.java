package com.tastyhouse.application.review.port.out;

import java.time.LocalDate;

public record ReviewBlindRequestSearchCondition(
    Long shopId,
    String status,
    String reason,
    LocalDate startDate,
    LocalDate endDate
) {

    public static ReviewBlindRequestSearchCondition of(
        Long shopId,
        String status,
        String reason,
        LocalDate startDate,
        LocalDate endDate
    ) {
        return new ReviewBlindRequestSearchCondition(shopId, status, reason, startDate, endDate);
    }
}
