package com.tastyhouse.core.domain.review.application.dto.result;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewsByRatingResult {
    private Map<Integer, List<LatestReviewListItemResult>> reviewsByRating;
    private List<LatestReviewListItemResult> allReviews;
    private Long totalReviewCount;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}
