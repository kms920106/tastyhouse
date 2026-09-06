package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;

public record ReviewBlindRequestHistoryResult(
    Long id,
    ReviewBlindReason reason,
    String detailReason,
    ReviewBlindStatus status,
    String rejectReason,
    LocalDateTime blindUntil,
    LocalDateTime createdAt
) {
}
