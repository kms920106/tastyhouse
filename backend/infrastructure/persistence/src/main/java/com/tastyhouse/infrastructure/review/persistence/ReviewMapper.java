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
     *
     * <p>{@code product_id}는 컬럼이 NOT NULL이지만, 삭제된 REVIEW_PRODUCT 애그리거트의 레거시 값으로
     * {@code 0}이 광범위하게 남아 있다. {@code ProductId} VO는 0을 양수가 아니라며 거부하므로, 0은 null과
     * 동일하게 "상품 미상"으로 취급해 승격을 건너뛴다.
     */
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
            domain.isHidden(),
            domain.isOwnerOnly(),
            domain.getDeliveryRating(),
            domain.getDeliveryComment()
        );
    }

    private static Long normalizeProductId(Long rawProductId) {
        return rawProductId == null || rawProductId <= 0 ? null : rawProductId;
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     *
     * <p>{@code ownerOnly}는 <b>의도적으로 복사하지 않는다</b> — 사장님만보기는 등록 시에만 정해지고
     * 전환이 불허라 update 대상이 아니다. 여기에 추가하면 전환 가능하다는 잘못된 신호가 된다.
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
            domain.isHidden(),
            domain.getDeliveryRating(),
            domain.getDeliveryComment()
        );
    }
}
