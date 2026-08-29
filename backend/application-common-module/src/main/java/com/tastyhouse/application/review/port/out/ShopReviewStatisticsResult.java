package com.tastyhouse.application.review.port.out;

import java.util.Map;

public record ShopReviewStatisticsResult(
    Long totalReviewCount,
    Double averageTasteRating,
    Double averageAmountRating,
    Double averagePriceRating,
    Double averageAtmosphereRating,
    Double averageKindnessRating,
    Double averageHygieneRating,
    Double willRevisitPercentage,
    Map<Integer, Long> ratingCounts,
    Map<Integer, Long> monthlyReviewCounts
) {
}
