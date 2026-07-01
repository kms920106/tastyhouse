package com.tastyhouse.core.domain.review.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.review.domain.vo.ReviewId;

public record ReviewLikedEvent(
    ReviewId reviewId,
    Long memberId,
    boolean liked,
    LocalDateTime occurredAt
) {
}
