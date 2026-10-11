package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;

public record ShopReviewSortTypeView(
    String sortType,
    String sortTypeDescription,
    LocalDateTime updatedAt
) {
}
