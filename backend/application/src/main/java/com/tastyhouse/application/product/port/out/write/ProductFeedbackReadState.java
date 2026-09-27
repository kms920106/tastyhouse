package com.tastyhouse.application.product.port.out.write;

import java.time.LocalDateTime;

public record ProductFeedbackReadState(
    Long id,
    Long shopId,
    LocalDateTime readAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
