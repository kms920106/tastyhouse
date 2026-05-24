package com.tastyhouse.core.domain.review.domain.event;

import com.tastyhouse.core.domain.review.domain.vo.ReviewId;

import java.time.LocalDateTime;

public record ReviewLikedEvent(
    ReviewId reviewId,
    Long memberId,
    boolean liked,
    LocalDateTime occurredAt
) {
}
