package com.tastyhouse.infrastructure.order.persistence;

import com.tastyhouse.domain.order.model.Order;

/**
 * 주문 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class OrderMapper {

    private OrderMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static Order toDomain(OrderJpaEntity entity) {
        return Order.reconstitute(
            entity.getId(),
            entity.getMemberId(),
            entity.getShopId(),
            entity.getOrderNumber(),
            entity.getOrderMethod(),
            entity.getOrderStatus(),
            entity.getOrdererName(),
            entity.getOrdererPhone(),
            entity.getOrdererEmail(),
            entity.getTotalProductAmount(),
            entity.getProductDiscountAmount(),
            entity.getCouponDiscountAmount(),
            entity.getPointDiscountAmount(),
            entity.getTotalDiscountAmount(),
            entity.getFinalAmount(),
            entity.getMemberCouponId(),
            entity.getUsedPoint(),
            entity.getEarnedPoint(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static OrderJpaEntity toEntity(Order domain) {
        return OrderJpaEntity.create(
            domain.getMemberId(),
            domain.getShopId(),
            domain.getOrderNumber(),
            domain.getOrderMethod(),
            domain.getOrderStatus(),
            domain.getOrdererName(),
            domain.getOrdererPhone(),
            domain.getOrdererEmail(),
            domain.getTotalProductAmount(),
            domain.getProductDiscountAmount(),
            domain.getCouponDiscountAmount(),
            domain.getPointDiscountAmount(),
            domain.getTotalDiscountAmount(),
            domain.getFinalAmount(),
            domain.getMemberCouponId(),
            domain.getUsedPoint(),
            domain.getEarnedPoint(),
            domain.isDeleted()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(OrderJpaEntity entity, Order domain) {
        entity.applyChanges(
            domain.getOrderStatus(),
            domain.getTotalProductAmount(),
            domain.getProductDiscountAmount(),
            domain.getCouponDiscountAmount(),
            domain.getPointDiscountAmount(),
            domain.getTotalDiscountAmount(),
            domain.getFinalAmount(),
            domain.getMemberCouponId(),
            domain.getUsedPoint(),
            domain.getEarnedPoint(),
            domain.isDeleted()
        );
    }
}
