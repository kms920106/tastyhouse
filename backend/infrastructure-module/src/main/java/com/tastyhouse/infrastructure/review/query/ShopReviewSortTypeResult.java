package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDateTime;

import com.tastyhouse.domain.review.model.ReviewSortType;

/**
 * 가게 리뷰 정렬 설정 조회 결과.
 */
public record ShopReviewSortTypeResult(
    ReviewSortType sortType,
    LocalDateTime updatedAt
) {
}
