package com.tastyhouse.infrastructure.persistence.review.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.model.ReviewReply;
import com.tastyhouse.domain.review.vo.ReviewCommentId;

final class ReviewReplyMapper {

    private ReviewReplyMapper() {
    }

    static ReviewReply toDomain(ReviewReplyJpaEntity entity) {
        return ReviewReply.reconstitute(
            entity.getId(),
            entity.getCommentId() == null ? null : ReviewCommentId.of(entity.getCommentId()),
            entity.getMemberId() == null ? null : MemberId.of(entity.getMemberId()),
            entity.getReplyToMemberId() == null ? null : MemberId.of(entity.getReplyToMemberId()),
            entity.getContent(),
            entity.isHidden(),
            entity.getCreatedAt()
        );
    }

    static ReviewReplyJpaEntity toEntity(ReviewReply reply) {
        return ReviewReplyJpaEntity.create(
            reply.getCommentId() == null ? null : reply.getCommentId().value(),
            reply.getMemberId() == null ? null : reply.getMemberId().value(),
            reply.getReplyToMemberId() == null ? null : reply.getReplyToMemberId().value(),
            reply.getContent(),
            reply.isHidden()
        );
    }

    static void applyChanges(ReviewReplyJpaEntity entity, ReviewReply reply) {
        entity.applyChanges(reply.isHidden());
    }
}
