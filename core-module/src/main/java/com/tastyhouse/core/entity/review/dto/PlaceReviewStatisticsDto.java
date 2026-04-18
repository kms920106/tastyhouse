package com.tastyhouse.core.entity.review.dto;

import java.util.Map;

public record PlaceReviewStatisticsDto(
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
