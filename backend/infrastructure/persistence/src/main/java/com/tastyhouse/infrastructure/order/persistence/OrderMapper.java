package com.tastyhouse.infrastructure.order.persistence;

import com.tastyhouse.domain.coupon.vo.MemberCouponId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class OrderMapper {
    private OrderMapper() {
    }

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
