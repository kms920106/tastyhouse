package com.tastyhouse.infrastructure.jpa.review.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.model.ReviewComment;
import com.tastyhouse.domain.review.vo.ReviewId;

final class ReviewCommentMapper {

    private ReviewCommentMapper() {
    }

    static ReviewComment toDomain(ReviewCommentJpaEntity entity) {
        return ReviewComment.reconstitute(
            entity.getId(),
            entity.getReviewId() == null ? null : ReviewId.of(entity.getReviewId()),
            entity.getMemberId() == null ? null : MemberId.of(entity.getMemberId()),
            entity.getContent(),
            entity.isHidden(),
            entity.getCreatedAt()
        );
    }

    static ReviewCommentJpaEntity toEntity(ReviewComment comment) {
        return ReviewCommentJpaEntity.create(
            comment.getReviewId() == null ? null : comment.getReviewId().value(),
            comment.getMemberId() == null ? null : comment.getMemberId().value(),
            comment.getContent(),
            comment.isHidden()
        );
    }

    static void applyChanges(ReviewCommentJpaEntity entity, ReviewComment comment) {
        entity.applyChanges(comment.isHidden());
    }
}
