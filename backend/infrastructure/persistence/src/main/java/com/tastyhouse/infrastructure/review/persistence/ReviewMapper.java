package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.application.review.port.out.write.ReviewState;

final class ReviewMapper {
    private ReviewMapper() {
    }

    static ReviewState toState(ReviewJpaEntity entity) {
        return new ReviewState(
            entity.getId(),
            entity.getShopId(),
            normalizeProductId(entity.getProductId()),
            entity.getMemberId(),
            entity.getContent(),
            entity.getTotalRating(),
            entity.getTasteRating(),
            entity.getAmountRating(),
            entity.getPriceRating(),
            entity.getAtmosphereRating(),
            entity.getKindnessRating(),
            entity.getHygieneRating(),
            entity.isWillRevisit(),
            entity.getOrderId(),
            entity.isHidden(),
            entity.isOwnerOnly(),
            entity.getDeliveryRating(),
            entity.getDeliveryComment(),
            entity.getCreatedAt()
        );
    }

    static ReviewJpaEntity toEntity(ReviewState state) {
        return ReviewJpaEntity.create(
            state.shopId(),
            state.productId(),
            state.memberId(),
            state.content(),
            state.totalRating(),
            state.tasteRating(),
            state.amountRating(),
            state.priceRating(),
            state.atmosphereRating(),
            state.kindnessRating(),
            state.hygieneRating(),
            state.willRevisit(),
            state.orderId(),
            state.hidden(),
            state.ownerOnly(),
            state.deliveryRating(),
            state.deliveryComment()
        );
    }

    private static Long normalizeProductId(Long rawProductId) {
        return rawProductId == null || rawProductId <= 0 ? null : rawProductId;
    }

    static void applyChanges(ReviewJpaEntity entity, ReviewState state) {
        entity.applyChanges(
            state.content(),
            state.totalRating(),
            state.tasteRating(),
            state.amountRating(),
            state.priceRating(),
            state.atmosphereRating(),
            state.kindnessRating(),
            state.hygieneRating(),
            state.willRevisit(),
            state.hidden(),
            state.deliveryRating(),
            state.deliveryComment()
        );
    }
}
