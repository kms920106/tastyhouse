package com.tastyhouse.infrastructure.order.persistence;

import com.tastyhouse.domain.coupon.vo.MemberCouponId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

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
            IdMapping.vo(entity.getMemberId(), MemberId::of),
            IdMapping.vo(entity.getShopId(), ShopId::of),
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
            entity.getDeliveryTipAmount(),
            entity.getCupDepositAmount(),
            entity.getFinalAmount(),
            entity.getDeliveryDestination(),
            entity.getSchedule(),
            IdMapping.vo(entity.getMemberCouponId(), MemberCouponId::of),
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
            IdMapping.raw(domain.getMemberId(), MemberId::value),
            IdMapping.raw(domain.getShopId(), ShopId::value),
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
            domain.getDeliveryTipAmount(),
            domain.getCupDepositAmount(),
            domain.getFinalAmount(),
            domain.getDeliveryDestination(),
            domain.getSchedule(),
            IdMapping.raw(domain.getMemberCouponId(), MemberCouponId::value),
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
            domain.getDeliveryTipAmount(),
            domain.getCupDepositAmount(),
            domain.getFinalAmount(),
            domain.getDeliveryDestination(),
            domain.getSchedule(),
            IdMapping.raw(domain.getMemberCouponId(), MemberCouponId::value),
            domain.getUsedPoint(),
            domain.getEarnedPoint(),
            domain.isDeleted()
        );
    }
}
