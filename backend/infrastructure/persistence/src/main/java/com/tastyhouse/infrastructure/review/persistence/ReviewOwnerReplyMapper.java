package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.review.model.ReviewOwnerReply;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ReviewOwnerReplyMapper {

    private ReviewOwnerReplyMapper() {
    }

    static ReviewOwnerReply toDomain(ReviewOwnerReplyJpaEntity entity) {
        return ReviewOwnerReply.reconstitute(
            entity.getId(),
            entity.getReviewId() == null ? null : ReviewId.of(entity.getReviewId()),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getCeoId() == null ? null : CeoId.of(entity.getCeoId()),
            entity.getContent(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ReviewOwnerReplyJpaEntity toEntity(ReviewOwnerReply reply) {
        return ReviewOwnerReplyJpaEntity.create(
            reply.getReviewId() == null ? null : reply.getReviewId().value(),
            reply.getShopId() == null ? null : reply.getShopId().value(),
            reply.getCeoId() == null ? null : reply.getCeoId().value(),
            reply.getContent()
        );
    }

    static void applyChanges(ReviewOwnerReplyJpaEntity entity, ReviewOwnerReply reply) {
        entity.applyChanges(reply.getContent());
    }
}
