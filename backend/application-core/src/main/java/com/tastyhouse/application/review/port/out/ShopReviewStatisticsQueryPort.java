package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;
import java.util.Map;

public interface ShopReviewStatisticsQueryPort {

    Double getAverageTotalRating(Long shopId, LocalDateTime from, LocalDateTime to);

    long countBetween(Long shopId, LocalDateTime from, LocalDateTime to);

    long countSince(Long shopId, LocalDateTime from);

    ShopReviewCategoryAverageResult getCategoryAverages(Long shopId, LocalDateTime from, LocalDateTime to);

    long countWillRevisitBetween(Long shopId, LocalDateTime from, LocalDateTime to);

    Map<Integer, Long> getRatingCounts(Long shopId, LocalDateTime from, LocalDateTime to);

    Map<String, Long> getMonthlyReviewCounts(Long shopId, LocalDateTime from, LocalDateTime to);

    Map<String, Double> getMonthlyAverageRatings(Long shopId, LocalDateTime from, LocalDateTime to);
}
