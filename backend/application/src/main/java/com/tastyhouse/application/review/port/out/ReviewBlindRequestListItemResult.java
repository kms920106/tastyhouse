package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;

public record ReviewBlindRequestListItemResult(
    Long id,
    Long reviewId,
    Long shopId,
    String shopName,
    ReviewBlindReason reason,
    ReviewBlindStatus status,
    LocalDateTime blindUntil,
    String reviewContent,
    Double reviewTotalRating,
    LocalDateTime createdAt
) {
}
