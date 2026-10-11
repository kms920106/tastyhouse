package com.tastyhouse.application.review.port.out;

import java.util.List;
import java.util.Map;

public record ShopReviewStatisticsOwnerResult(
    boolean hasData,
    Double averageTotalRating,
    Long totalReviewCount,
    Long recentReviewCount,
    Map<Integer, Long> ratingCounts,
    Double averageTasteRating,
    Double averageAmountRating,
    Double averagePriceRating,
    Double averageAtmosphereRating,
    Double averageKindnessRating,
    Double averageHygieneRating,
    Double willRevisitPercentage,
    List<ShopReviewMonthlyStatResult> monthlyStats
) {
}
