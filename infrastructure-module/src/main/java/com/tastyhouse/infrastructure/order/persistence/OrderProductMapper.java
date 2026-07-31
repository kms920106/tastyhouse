package com.tastyhouse.infrastructure.order.persistence;

import com.tastyhouse.domain.order.domain.model.OrderProduct;

/**
 * 주문 상품 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class OrderProductMapper {

    private OrderProductMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static OrderProduct toDomain(OrderProductJpaEntity entity) {
        return OrderProduct.reconstitute(
            entity.getId(),
            entity.getOrderId(),
            entity.getProductId(),
            entity.getName(),
            entity.getImageUrl(),
            entity.getQuantity(),
            entity.getOriginalPrice(),
            entity.getDiscountPrice(),
            entity.getTotalOptionPrice(),
            entity.getTotalPrice()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static OrderProductJpaEntity toEntity(OrderProduct domain) {
        return OrderProductJpaEntity.create(
            domain.getOrderId(),
            domain.getProductId(),
            domain.getName(),
            domain.getImageUrl(),
            domain.getQuantity(),
            domain.getOriginalPrice(),
            domain.getDiscountPrice(),
            domain.getTotalOptionPrice(),
            domain.getTotalPrice()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(OrderProductJpaEntity entity, OrderProduct domain) {
        entity.applyChanges(
            domain.getTotalOptionPrice(),
            domain.getTotalPrice()
        );
    }
}
