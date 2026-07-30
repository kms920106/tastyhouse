package com.tastyhouse.infrastructure.review.query;

public record ProductReviewStatisticsResult(
    Long totalReviewCount,
    Double averageTasteRating,
    Double averageAmountRating,
    Double averagePriceRating
) {
}
