package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.model.ReviewComment;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ReviewCommentMapper {
    private ReviewCommentMapper() {
    }

    static ReviewComment toDomain(ReviewCommentJpaEntity entity) {
        return ReviewComment.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getReviewId(), ReviewId::of),
            IdMapping.vo(entity.getMemberId(), MemberId::of),
            entity.getContent(),
            entity.isHidden(),
            entity.getCreatedAt()
        );
    }

    static ReviewCommentJpaEntity toEntity(ReviewComment domain) {
        return ReviewCommentJpaEntity.create(
            IdMapping.raw(domain.getReviewId(), ReviewId::value),
            IdMapping.raw(domain.getMemberId(), MemberId::value),
            domain.getContent(),
            domain.isHidden()
        );
    }

    static void applyChanges(ReviewCommentJpaEntity entity, ReviewComment domain) {
        entity.applyChanges(domain.isHidden());
    }
}
