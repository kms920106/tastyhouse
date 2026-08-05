package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 리뷰 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ReviewMapper {

    private ReviewMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static Review toDomain(ReviewJpaEntity entity) {
        return Review.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(entity.getProductId(), ProductId::of),
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
            entity.getCreatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
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
            domain.isHidden()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
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
            domain.isHidden()
        );
    }
}
