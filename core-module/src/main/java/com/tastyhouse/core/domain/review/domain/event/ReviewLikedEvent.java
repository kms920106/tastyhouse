package com.tastyhouse.core.domain.review.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.review.domain.vo.ReviewId;

public record ReviewLikedEvent(
    ReviewId reviewId,
    MemberId memberId,
    boolean liked,
    LocalDateTime occurredAt
) {
}
