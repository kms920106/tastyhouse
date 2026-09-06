package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.review.model.ReviewOwnerReply;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ReviewOwnerReplyMapper {
    private ReviewOwnerReplyMapper() {
    }

    static ReviewOwnerReply toDomain(ReviewOwnerReplyJpaEntity entity) {
        return ReviewOwnerReply.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getReviewId(), ReviewId::of),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(entity.getCeoId(), CeoId::of),
            entity.getContent(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ReviewOwnerReplyJpaEntity toEntity(ReviewOwnerReply domain) {
        return ReviewOwnerReplyJpaEntity.create(
            IdMapping.raw(domain.getReviewId(), ReviewId::value),
            IdMapping.raw(domain.getShopId(), ShopId::value),
            IdMapping.raw(domain.getCeoId(), CeoId::value),
            domain.getContent()
        );
    }

    static void applyChanges(ReviewOwnerReplyJpaEntity entity, ReviewOwnerReply domain) {
        entity.applyChanges(domain.getContent());
    }
}
