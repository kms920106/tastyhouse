package com.tastyhouse.infrastructure.order.persistence;

import com.tastyhouse.application.order.port.out.write.OrderDeliveryDestinationSnapshot;
import com.tastyhouse.application.order.port.out.write.OrderScheduleSnapshot;
import com.tastyhouse.application.order.port.out.write.OrderState;

final class OrderMapper {
    private OrderMapper() {
    }

    static OrderState toState(OrderJpaEntity entity) {
        return new OrderState(
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
            entity.getDeliveryTipAmount(),
            entity.getCupDepositAmount(),
            entity.getFinalAmount(),
            toSnapshot(entity.getDeliveryDestination()),
            toSnapshot(entity.getSchedule()),
            entity.getMemberCouponId(),
            entity.getUsedPoint(),
            entity.getEarnedPoint(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static OrderJpaEntity toEntity(OrderState state) {
        return OrderJpaEntity.create(
            state.memberId(),
            state.shopId(),
            state.orderNumber(),
            state.orderMethod(),
            state.orderStatus(),
            state.ordererName(),
            state.ordererPhone(),
            state.ordererEmail(),
            state.totalProductAmount(),
            state.productDiscountAmount(),
            state.couponDiscountAmount(),
            state.pointDiscountAmount(),
            state.totalDiscountAmount(),
            state.deliveryTipAmount(),
            state.cupDepositAmount(),
            state.finalAmount(),
            toEmbeddable(state.deliveryDestination()),
            toEmbeddable(state.schedule()),
            state.memberCouponId(),
            state.usedPoint(),
            state.earnedPoint(),
            state.deleted()
        );
    }

    static void applyChanges(OrderJpaEntity entity, OrderState state) {
        entity.applyChanges(
            state.orderStatus(),
            state.totalProductAmount(),
            state.productDiscountAmount(),
            state.couponDiscountAmount(),
            state.pointDiscountAmount(),
            state.totalDiscountAmount(),
            state.deliveryTipAmount(),
            state.cupDepositAmount(),
            state.finalAmount(),
            toEmbeddable(state.deliveryDestination()),
            toEmbeddable(state.schedule()),
            state.memberCouponId(),
            state.usedPoint(),
            state.earnedPoint(),
            state.deleted()
        );
    }

    private static OrderDeliveryDestinationSnapshot toSnapshot(OrderDeliveryDestinationEmbeddable embeddable) {
        return embeddable == null
            ? null
            : new OrderDeliveryDestinationSnapshot(
                embeddable.adminDongId(),
                embeddable.detailAddress(),
                embeddable.distanceMeters(),
                embeddable.latitude(),
                embeddable.longitude(),
                embeddable.lotAddress(),
                embeddable.roadAddress()
            );
    }

    private static OrderDeliveryDestinationEmbeddable toEmbeddable(OrderDeliveryDestinationSnapshot snapshot) {
        return snapshot == null
            ? null
            : new OrderDeliveryDestinationEmbeddable(
                snapshot.adminDongId(),
                snapshot.detailAddress(),
                snapshot.distanceMeters(),
                snapshot.latitude(),
                snapshot.longitude(),
                snapshot.lotAddress(),
                snapshot.roadAddress()
            );
    }

    private static OrderScheduleSnapshot toSnapshot(OrderScheduleEmbeddable embeddable) {
        return embeddable == null
            ? null
            : new OrderScheduleSnapshot(embeddable.scheduledAt(), embeddable.scheduledSlotEndAt());
    }

    private static OrderScheduleEmbeddable toEmbeddable(OrderScheduleSnapshot snapshot) {
        return snapshot == null
            ? null
            : new OrderScheduleEmbeddable(snapshot.scheduledAt(), snapshot.scheduledSlotEndAt());
    }
}
