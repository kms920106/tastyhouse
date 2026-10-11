package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;

public record ShopReviewSortTypeResult(
    String sortType,
    LocalDateTime updatedAt
) {
}
