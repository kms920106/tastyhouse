package com.tastyhouse.application.review.port.out;

import java.time.LocalDate;

import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;

public record ReviewBlindRequestSearchCondition(
    Long shopId,
    ReviewBlindStatus status,
    ReviewBlindReason reason,
    LocalDate startDate,
    LocalDate endDate
) {

    public static ReviewBlindRequestSearchCondition of(
        Long shopId,
        ReviewBlindStatus status,
        ReviewBlindReason reason,
        LocalDate startDate,
        LocalDate endDate
    ) {
        return new ReviewBlindRequestSearchCondition(shopId, status, reason, startDate, endDate);
    }
}
