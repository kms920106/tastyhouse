package com.tastyhouse.core.common;

import com.tastyhouse.core.entity.review.dto.LatestReviewListItemDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ReviewsByRatingResult {
    private Map<Integer, List<LatestReviewListItemDto>> reviewsByRating;
    private List<LatestReviewListItemDto> allReviews;
    private Long totalReviewCount;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}
