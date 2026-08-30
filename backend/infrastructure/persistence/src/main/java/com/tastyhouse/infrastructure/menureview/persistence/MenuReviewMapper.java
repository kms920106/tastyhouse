package com.tastyhouse.infrastructure.menureview.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.model.MenuReview;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 메뉴 평가 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을
 * infrastructure에 둔다. VO↔raw 변환은 예외 없이 {@link IdMapping}으로 통일한다.
 */
final class MenuReviewMapper {

    private MenuReviewMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static MenuReview toDomain(MenuReviewJpaEntity entity) {
        return MenuReview.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getMemberId(), MemberId::of),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            IdMapping.vo(entity.getOrderId(), OrderId::of),
            IdMapping.vo(entity.getOrderProductId(), OrderProductId::of),
            entity.getRating(),
            entity.getComment(),
            entity.isHidden(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static MenuReviewJpaEntity toEntity(MenuReview domain) {
        return MenuReviewJpaEntity.create(
            IdMapping.raw(domain.getMemberId(), MemberId::value),
            IdMapping.raw(domain.getShopId(), ShopId::value),
            IdMapping.raw(domain.getProductId(), ProductId::value),
            IdMapping.raw(domain.getOrderId(), OrderId::value),
            IdMapping.raw(domain.getOrderProductId(), OrderProductId::value),
            domain.getRating(),
            domain.getComment(),
            domain.isHidden()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(MenuReviewJpaEntity entity, MenuReview domain) {
        entity.applyChanges(
            domain.getRating(),
            domain.getComment(),
            domain.isHidden()
        );
    }
}
