package com.tastyhouse.core.domain.review.domain.event;

import com.tastyhouse.core.domain.review.domain.vo.ReviewId;

import java.time.LocalDateTime;

public record ReviewCreatedEvent(
    ReviewId reviewId,
    Long memberId,
    Long placeId,
    Long productId,
    LocalDateTime occurredAt
) {
}
