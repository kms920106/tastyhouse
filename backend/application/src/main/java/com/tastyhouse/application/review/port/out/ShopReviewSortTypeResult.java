package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.review.model.ReviewSortType;

public record ShopReviewSortTypeResult(
    ReviewSortType sortType,
    LocalDateTime updatedAt
) {
}
