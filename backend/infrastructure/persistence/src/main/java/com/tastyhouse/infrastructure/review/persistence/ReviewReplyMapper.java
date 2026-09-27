package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.application.review.port.out.write.ReviewReplyState;

final class ReviewReplyMapper {
    private ReviewReplyMapper() {
    }

    static ReviewReplyState toState(ReviewReplyJpaEntity entity) {
        return new ReviewReplyState(
            entity.getId(),
            entity.getCommentId(),
            entity.getMemberId(),
            entity.getReplyToMemberId(),
            entity.getContent(),
            entity.isHidden(),
            entity.getCreatedAt()
        );
    }

    static ReviewReplyJpaEntity toEntity(ReviewReplyState state) {
        return ReviewReplyJpaEntity.create(
            state.commentId(),
            state.memberId(),
            state.replyToMemberId(),
            state.content(),
            state.hidden()
        );
    }

    static void applyChanges(ReviewReplyJpaEntity entity, ReviewReplyState state) {
        entity.applyChanges(state.hidden());
    }
}
