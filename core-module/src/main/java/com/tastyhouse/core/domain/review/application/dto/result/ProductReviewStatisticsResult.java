package com.tastyhouse.core.domain.review.application.dto.result;

public record ProductReviewStatisticsResult(
    Long totalReviewCount,
    Double averageTasteRating,
    Double averageAmountRating,
    Double averagePriceRating
) {
}
