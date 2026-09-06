package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ReviewBlindRequestMapper {
    private ReviewBlindRequestMapper() {
    }

    static ReviewBlindRequest toDomain(ReviewBlindRequestJpaEntity entity) {
        return ReviewBlindRequest.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getReviewId(), ReviewId::of),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(entity.getCeoId(), CeoId::of),
            entity.getReason(),
            entity.getDetailReason(),
            entity.getStatus(),
            entity.getRejectReason(),
            entity.getBlindUntil(),
            entity.getCreatedAt()
        );
    }

    static ReviewBlindRequestJpaEntity toEntity(ReviewBlindRequest domain) {
        return ReviewBlindRequestJpaEntity.create(
            IdMapping.raw(domain.getReviewId(), ReviewId::value),
            IdMapping.raw(domain.getShopId(), ShopId::value),
            IdMapping.raw(domain.getCeoId(), CeoId::value),
            domain.getReason(),
            domain.getDetailReason(),
            domain.getStatus(),
            domain.getRejectReason(),
            domain.getBlindUntil()
        );
    }

    static void applyChanges(ReviewBlindRequestJpaEntity entity, ReviewBlindRequest domain) {
        entity.applyChanges(
            domain.getStatus(),
            domain.getRejectReason(),
            domain.getBlindUntil()
        );
    }
}
