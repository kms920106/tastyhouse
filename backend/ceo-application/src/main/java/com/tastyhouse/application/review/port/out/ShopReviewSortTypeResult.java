package com.tastyhouse.application.review.port.out;

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
