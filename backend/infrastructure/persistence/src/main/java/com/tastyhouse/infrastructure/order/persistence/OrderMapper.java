package com.tastyhouse.infrastructure.order.persistence;

import com.tastyhouse.domain.coupon.vo.MemberCouponId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.vo.OrderDeliveryDestination;
import com.tastyhouse.domain.order.vo.OrderSchedule;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.vo.ShopId;

final class OrderMapper {
    private OrderMapper() {
    }

    static Order toDomain(OrderJpaEntity entity) {
        return Order.reconstitute(
            entity.getId(),
            entity.getMemberId() == null ? null : MemberId.of(entity.getMemberId()),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getOrderNumber(),
            entity.getOrderMethod() == null ? null : OrderMethod.valueOf(entity.getOrderMethod()),
            entity.getOrderStatus() == null ? null : OrderStatus.valueOf(entity.getOrderStatus()),
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
            toDeliveryDestination(entity.getDeliveryDestination()),
            toSchedule(entity.getSchedule()),
            entity.getMemberCouponId() == null ? null : MemberCouponId.of(entity.getMemberCouponId()),
            entity.getUsedPoint(),
            entity.getEarnedPoint(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static OrderJpaEntity toEntity(Order order) {
        return OrderJpaEntity.create(
            order.getMemberId() == null ? null : order.getMemberId().value(),
            order.getShopId() == null ? null : order.getShopId().value(),
            order.getOrderNumber(),
            order.getOrderMethod() == null ? null : order.getOrderMethod().name(),
            order.getOrderStatus() == null ? null : order.getOrderStatus().name(),
            order.getOrdererName(),
            order.getOrdererPhone(),
            order.getOrdererEmail(),
            order.getTotalProductAmount(),
            order.getProductDiscountAmount(),
            order.getCouponDiscountAmount(),
            order.getPointDiscountAmount(),
            order.getTotalDiscountAmount(),
            order.getDeliveryTipAmount(),
            order.getCupDepositAmount(),
            order.getFinalAmount(),
            toEmbeddable(order.getDeliveryDestination()),
            toEmbeddable(order.getSchedule()),
            order.getMemberCouponId() == null ? null : order.getMemberCouponId().value(),
            order.getUsedPoint(),
            order.getEarnedPoint(),
            order.isDeleted()
        );
    }

    static void applyChanges(OrderJpaEntity entity, Order order) {
        entity.applyChanges(
            order.getOrderStatus() == null ? null : order.getOrderStatus().name(),
            order.getTotalProductAmount(),
            order.getProductDiscountAmount(),
            order.getCouponDiscountAmount(),
            order.getPointDiscountAmount(),
            order.getTotalDiscountAmount(),
            order.getDeliveryTipAmount(),
            order.getCupDepositAmount(),
            order.getFinalAmount(),
            toEmbeddable(order.getDeliveryDestination()),
            toEmbeddable(order.getSchedule()),
            order.getMemberCouponId() == null ? null : order.getMemberCouponId().value(),
            order.getUsedPoint(),
            order.getEarnedPoint(),
            order.isDeleted()
        );
    }

    private static OrderDeliveryDestination toDeliveryDestination(OrderDeliveryDestinationEmbeddable embeddable) {
        return embeddable == null
            ? null
            : new OrderDeliveryDestination(
                embeddable.adminDongId(),
                embeddable.detailAddress(),
                embeddable.distanceMeters(),
                embeddable.latitude(),
                embeddable.longitude(),
                embeddable.lotAddress(),
                embeddable.roadAddress()
            );
    }

    private static OrderDeliveryDestinationEmbeddable toEmbeddable(OrderDeliveryDestination destination) {
        return destination == null
            ? null
            : new OrderDeliveryDestinationEmbeddable(
                destination.adminDongId(),
                destination.detailAddress(),
                destination.distanceMeters(),
                destination.latitude(),
                destination.longitude(),
                destination.lotAddress(),
                destination.roadAddress()
            );
    }

    private static OrderSchedule toSchedule(OrderScheduleEmbeddable embeddable) {
        return embeddable == null ? null : new OrderSchedule(embeddable.scheduledAt(), embeddable.scheduledSlotEndAt());
    }

    private static OrderScheduleEmbeddable toEmbeddable(OrderSchedule schedule) {
        return schedule == null ? null : new OrderScheduleEmbeddable(schedule.scheduledAt(), schedule.scheduledSlotEndAt());
    }
}
