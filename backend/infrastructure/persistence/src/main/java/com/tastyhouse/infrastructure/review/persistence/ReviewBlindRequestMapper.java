package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ReviewBlindRequestMapper {
    private ReviewBlindRequestMapper() {
    }

    static ReviewBlindRequest toDomain(ReviewBlindRequestJpaEntity entity) {
        return ReviewBlindRequest.reconstitute(
            entity.getId(),
            entity.getReviewId() == null ? null : ReviewId.of(entity.getReviewId()),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getCeoId() == null ? null : CeoId.of(entity.getCeoId()),
            entity.getReason() == null ? null : ReviewBlindReason.valueOf(entity.getReason()),
            entity.getDetailReason(),
            entity.getStatus() == null ? null : ReviewBlindStatus.valueOf(entity.getStatus()),
            entity.getRejectReason(),
            entity.getBlindUntil(),
            entity.getCreatedAt()
        );
    }

    static ReviewBlindRequestJpaEntity toEntity(ReviewBlindRequest request) {
        return ReviewBlindRequestJpaEntity.create(
            request.getReviewId() == null ? null : request.getReviewId().value(),
            request.getShopId() == null ? null : request.getShopId().value(),
            request.getCeoId() == null ? null : request.getCeoId().value(),
            request.getReason() == null ? null : request.getReason().name(),
            request.getDetailReason(),
            request.getStatus() == null ? null : request.getStatus().name(),
            request.getRejectReason(),
            request.getBlindUntil()
        );
    }

    static void applyChanges(ReviewBlindRequestJpaEntity entity, ReviewBlindRequest request) {
        entity.applyChanges(
            request.getStatus() == null ? null : request.getStatus().name(),
            request.getRejectReason(),
            request.getBlindUntil()
        );
    }
}
