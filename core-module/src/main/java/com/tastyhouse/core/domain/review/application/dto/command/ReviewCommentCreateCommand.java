package com.tastyhouse.core.domain.review.application.dto.command;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.review.domain.vo.ReviewId;

public record ReviewCommentCreateCommand(
    ReviewId reviewId,
    MemberId memberId,
    String content
) {

    public static ReviewCommentCreateCommand of(ReviewId reviewId, MemberId memberId, String content) {
        return new ReviewCommentCreateCommand(reviewId, memberId, content);
    }
}
