package com.tastyhouse.application.review.port.out.write;

import java.time.LocalDateTime;

public record ReviewOwnerReplyState(
    Long id,
    Long reviewId,
    Long shopId,
    Long ceoId,
    String content,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
