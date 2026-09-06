package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ReviewMapper {
    private ReviewMapper() {
    }

    static Review toDomain(ReviewJpaEntity entity) {
        return Review.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(normalizeProductId(entity.getProductId()), ProductId::of),
            IdMapping.vo(entity.getMemberId(), MemberId::of),
            entity.getContent(),
            entity.getTotalRating(),
            entity.getTasteRating(),
            entity.getAmountRating(),
            entity.getPriceRating(),
            entity.getAtmosphereRating(),
            entity.getKindnessRating(),
            entity.getHygieneRating(),
            entity.isWillRevisit(),
            IdMapping.vo(entity.getOrderId(), OrderId::of),
            entity.isHidden(),
            entity.isOwnerOnly(),
            entity.getDeliveryRating(),
            entity.getDeliveryComment(),
            entity.getCreatedAt()
        );
    }

    static ReviewJpaEntity toEntity(Review domain) {
        return ReviewJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            IdMapping.raw(domain.getProductId(), ProductId::value),
            IdMapping.raw(domain.getMemberId(), MemberId::value),
            domain.getContent(),
            domain.getTotalRating(),
            domain.getTasteRating(),
            domain.getAmountRating(),
            domain.getPriceRating(),
            domain.getAtmosphereRating(),
            domain.getKindnessRating(),
            domain.getHygieneRating(),
            domain.isWillRevisit(),
            IdMapping.raw(domain.getOrderId(), OrderId::value),
            domain.isHidden(),
            domain.isOwnerOnly(),
            domain.getDeliveryRating(),
            domain.getDeliveryComment()
        );
    }

    private static Long normalizeProductId(Long rawProductId) {
        return rawProductId == null || rawProductId <= 0 ? null : rawProductId;
    }

    static void applyChanges(ReviewJpaEntity entity, Review domain) {
        entity.applyChanges(
            domain.getContent(),
            domain.getTotalRating(),
            domain.getTasteRating(),
            domain.getAmountRating(),
            domain.getPriceRating(),
            domain.getAtmosphereRating(),
            domain.getKindnessRating(),
            domain.getHygieneRating(),
            domain.isWillRevisit(),
            domain.isHidden(),
            domain.getDeliveryRating(),
            domain.getDeliveryComment()
        );
    }
}
