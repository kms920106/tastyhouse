package com.tastyhouse.core.domain.review.application.dto.command;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.review.domain.vo.ReviewId;

public record ReviewDeleteCommand(
    ReviewId reviewId,
    MemberId memberId,
    Long productId
) {

    public static ReviewDeleteCommand of(ReviewId reviewId, MemberId memberId, Long productId) {
        return new ReviewDeleteCommand(reviewId, memberId, productId);
    }
}
