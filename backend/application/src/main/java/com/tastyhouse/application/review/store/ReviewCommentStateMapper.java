package com.tastyhouse.application.review.store;

import com.tastyhouse.application.review.port.out.write.ReviewCommentState;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.model.ReviewComment;
import com.tastyhouse.domain.review.vo.ReviewId;

final class ReviewCommentStateMapper {
    private ReviewCommentStateMapper() {
    }

    static ReviewComment toDomain(ReviewCommentState state) {
        return ReviewComment.reconstitute(
            state.id(),
            state.reviewId() == null ? null : ReviewId.of(state.reviewId()),
            state.memberId() == null ? null : MemberId.of(state.memberId()),
            state.content(),
            state.hidden(),
            state.createdAt()
        );
    }

    static ReviewCommentState toState(ReviewComment comment) {
        return new ReviewCommentState(
            comment.getId(),
            comment.getReviewId() == null ? null : comment.getReviewId().value(),
            comment.getMemberId() == null ? null : comment.getMemberId().value(),
            comment.getContent(),
            comment.isHidden(),
            comment.getCreatedAt()
        );
    }
}
