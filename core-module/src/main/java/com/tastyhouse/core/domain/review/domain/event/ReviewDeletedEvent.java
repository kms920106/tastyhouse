package com.tastyhouse.core.domain.review.domain.event;

import com.tastyhouse.core.domain.review.domain.vo.ReviewId;

import java.time.LocalDateTime;

public record ReviewDeletedEvent(
    ReviewId reviewId,
    Long memberId,
    Long productId,
    LocalDateTime occurredAt
) {
}
