package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.model.ReviewReply;
import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ReviewReplyMapper {
    private ReviewReplyMapper() {
    }

    static ReviewReply toDomain(ReviewReplyJpaEntity entity) {
        return ReviewReply.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getCommentId(), ReviewCommentId::of),
            IdMapping.vo(entity.getMemberId(), MemberId::of),
            IdMapping.vo(entity.getReplyToMemberId(), MemberId::of),
            entity.getContent(),
            entity.isHidden(),
            entity.getCreatedAt()
        );
    }

    static ReviewReplyJpaEntity toEntity(ReviewReply domain) {
        return ReviewReplyJpaEntity.create(
            IdMapping.raw(domain.getCommentId(), ReviewCommentId::value),
            IdMapping.raw(domain.getMemberId(), MemberId::value),
            IdMapping.raw(domain.getReplyToMemberId(), MemberId::value),
            domain.getContent(),
            domain.isHidden()
        );
    }

    static void applyChanges(ReviewReplyJpaEntity entity, ReviewReply domain) {
        entity.applyChanges(domain.isHidden());
    }
}
