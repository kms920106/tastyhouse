package com.tastyhouse.application.review.port.out;

import java.util.Map;

public interface ReviewStatisticsQueryPort {

    Long countVisibleByShopId(Long shopId);

    Long countWillRevisit(Long shopId);

    Double getAverageTasteRating(Long shopId);

    Double getAverageAmountRating(Long shopId);

    Double getAveragePriceRating(Long shopId);

    Double getAverageAtmosphereRating(Long shopId);

    Double getAverageKindnessRating(Long shopId);

    Double getAverageHygieneRating(Long shopId);

    Map<Integer, Long> getRatingCounts(Long shopId);

    Map<Integer, Long> getMonthlyReviewCounts(Long shopId, int year);

    Long countVisibleByProductId(Long productId);

    Double getAverageTasteRatingByProductId(Long productId);

    Double getAverageAmountRatingByProductId(Long productId);

    Double getAveragePriceRatingByProductId(Long productId);

    long countVisibleReviewsByMemberId(Long memberId);
}
