package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ReviewMapper {
    private ReviewMapper() {
    }

    static Review toDomain(ReviewJpaEntity entity) {
        Long productId = normalizeProductId(entity.getProductId());
        return Review.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            productId == null ? null : ProductId.of(productId),
            entity.getMemberId() == null ? null : MemberId.of(entity.getMemberId()),
            entity.getContent(),
            entity.getTotalRating(),
            entity.getTasteRating(),
            entity.getAmountRating(),
            entity.getPriceRating(),
            entity.getAtmosphereRating(),
            entity.getKindnessRating(),
            entity.getHygieneRating(),
            entity.isWillRevisit(),
            entity.getOrderId() == null ? null : OrderId.of(entity.getOrderId()),
            entity.isHidden(),
            entity.isOwnerOnly(),
            entity.getDeliveryRating(),
            entity.getDeliveryComment(),
            entity.getCreatedAt()
        );
    }

    static ReviewJpaEntity toEntity(Review review) {
        return ReviewJpaEntity.create(
            review.getShopId() == null ? null : review.getShopId().value(),
            review.getProductId() == null ? null : review.getProductId().value(),
            review.getMemberId() == null ? null : review.getMemberId().value(),
            review.getContent(),
            review.getTotalRating(),
            review.getTasteRating(),
            review.getAmountRating(),
            review.getPriceRating(),
            review.getAtmosphereRating(),
            review.getKindnessRating(),
            review.getHygieneRating(),
            review.isWillRevisit(),
            review.getOrderId() == null ? null : review.getOrderId().value(),
            review.isHidden(),
            review.isOwnerOnly(),
            review.getDeliveryRating(),
            review.getDeliveryComment()
        );
    }

    private static Long normalizeProductId(Long rawProductId) {
        return rawProductId == null || rawProductId <= 0 ? null : rawProductId;
    }

    static void applyChanges(ReviewJpaEntity entity, Review review) {
        entity.applyChanges(
            review.getContent(),
            review.getTotalRating(),
            review.getTasteRating(),
            review.getAmountRating(),
            review.getPriceRating(),
            review.getAtmosphereRating(),
            review.getKindnessRating(),
            review.getHygieneRating(),
            review.isWillRevisit(),
            review.isHidden(),
            review.getDeliveryRating(),
            review.getDeliveryComment()
        );
    }
}
