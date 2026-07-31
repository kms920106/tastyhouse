package com.tastyhouse.domain.review.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.review.domain.vo.ReviewId;

public record ReviewCreatedEvent(
    ReviewId reviewId,
    MemberId memberId,
    Long shopId,
    Long productId,
    LocalDateTime occurredAt
) {
}
