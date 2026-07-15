package com.tastyhouse.core.domain.review.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.review.domain.vo.ReviewId;

public record ReviewDeletedEvent(
    ReviewId reviewId,
    MemberId memberId,
    Long productId,
    LocalDateTime occurredAt
) {
}
