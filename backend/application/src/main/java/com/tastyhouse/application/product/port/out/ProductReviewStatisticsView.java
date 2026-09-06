package com.tastyhouse.application.product.port.out;

public record ProductReviewStatisticsView(
    Double totalRating,
    Long totalReviewCount,
    Double averageTasteRating,
    Double averageAmountRating,
    Double averagePriceRating
) {
}
