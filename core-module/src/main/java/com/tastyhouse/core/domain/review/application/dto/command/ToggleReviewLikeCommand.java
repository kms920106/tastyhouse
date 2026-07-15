package com.tastyhouse.core.domain.review.application.dto.command;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.review.domain.vo.ReviewId;

public record ToggleReviewLikeCommand(
    ReviewId reviewId,
    MemberId memberId
) {

    public static ToggleReviewLikeCommand of(ReviewId reviewId, MemberId memberId) {
        return new ToggleReviewLikeCommand(reviewId, memberId);
    }
}
