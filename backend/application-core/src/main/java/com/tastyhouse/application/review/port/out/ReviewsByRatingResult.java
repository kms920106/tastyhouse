package com.tastyhouse.application.review.port.out;

import java.util.List;
import java.util.Map;

public record ReviewsByRatingResult(
    Map<Integer, List<LatestReviewListItemResult>> reviewsByRating,
    List<LatestReviewListItemResult> allReviews,
    Long totalReviewCount,
    long totalElements,
    int totalPages,
    int currentPage,
    int pageSize
) {
}
