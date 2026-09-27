package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;

public record ReviewBlindRequestListItemResult(
    Long id,
    Long reviewId,
    Long shopId,
    String shopName,
    String reason,
    String reasonDescription,
    String status,
    String statusDescription,
    LocalDateTime blindUntil,
    String reviewContent,
    Double reviewTotalRating,
    LocalDateTime createdAt
) {

    public ReviewBlindRequestListItemResult withDescriptions(String reasonDescription, String statusDescription) {
        return new ReviewBlindRequestListItemResult(
            this.id,
            this.reviewId,
            this.shopId,
            this.shopName,
            this.reason,
            reasonDescription,
            this.status,
            statusDescription,
            this.blindUntil,
            this.reviewContent,
            this.reviewTotalRating,
            this.createdAt
        );
    }
}
