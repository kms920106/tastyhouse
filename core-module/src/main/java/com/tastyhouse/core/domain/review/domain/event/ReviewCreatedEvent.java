package com.tastyhouse.core.domain.review.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.review.domain.vo.ReviewId;

public record ReviewCreatedEvent(
    ReviewId reviewId,
    Long memberId,
    Long shopId,
    Long productId,
    LocalDateTime occurredAt
) {
}
