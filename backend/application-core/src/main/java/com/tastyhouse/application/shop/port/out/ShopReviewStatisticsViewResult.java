package com.tastyhouse.application.shop.port.out;

import java.util.Map;

public record ShopReviewStatisticsViewResult(
    Double totalRating,
    Long totalReviewCount,
    Double averageTasteRating,
    Double averageAmountRating,
    Double averagePriceRating,
    Double averageAtmosphereRating,
    Double averageKindnessRating,
    Double averageHygieneRating,
    Double willRevisitPercentage,
    Map<Integer, Long> monthlyReviewCounts,
    Map<Integer, Long> ratingCounts
) {
}
