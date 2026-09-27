package com.tastyhouse.application.review.port.out.write;

import java.time.LocalDateTime;

public record ShopReviewDisplaySettingState(
    Long id,
    Long shopId,
    String sortType,
    LocalDateTime updatedAt
) {
}
