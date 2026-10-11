package com.tastyhouse.application.review.port.out;

public record ProductReviewStatisticsResult(
    Long totalReviewCount,
    Double averageTasteRating,
    Double averageAmountRating,
    Double averagePriceRating
) {
}
