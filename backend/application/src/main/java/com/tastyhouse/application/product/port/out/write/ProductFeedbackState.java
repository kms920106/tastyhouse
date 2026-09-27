package com.tastyhouse.application.product.port.out.write;

import java.time.LocalDateTime;

public record ProductFeedbackState(
    Long id,
    Long productId,
    Long shopId,
    Long memberId,
    String feedbackType,
    String content,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
