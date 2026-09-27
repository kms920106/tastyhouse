package com.tastyhouse.application.menureview.port.out.write;

import java.time.LocalDateTime;

public record MenuReviewState(
    Long id,
    Long memberId,
    Long shopId,
    Long productId,
    Long orderId,
    Long orderProductId,
    Integer rating,
    String comment,
    boolean hidden,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
