package com.tastyhouse.domain.review.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;
import com.tastyhouse.domain.review.vo.ReviewId;

public record ReviewBlindApprovedEvent(
    ReviewId reviewId,
    MemberId reviewerMemberId,
    ReviewBlindRequestId blindRequestId,
    LocalDateTime blindUntil,
    LocalDateTime occurredAt
) {
    public static ReviewBlindApprovedEvent of(
        ReviewId reviewId,
        MemberId reviewerMemberId,
        ReviewBlindRequestId blindRequestId,
        LocalDateTime blindUntil,
        LocalDateTime occurredAt
    ) {
        return new ReviewBlindApprovedEvent(
            reviewId,
            reviewerMemberId,
            blindRequestId,
            blindUntil,
            occurredAt
        );
    }
}
