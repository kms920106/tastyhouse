package com.tastyhouse.application.order.store;

import com.tastyhouse.application.order.port.out.write.OrderDeliveryDestinationSnapshot;
import com.tastyhouse.application.order.port.out.write.OrderScheduleSnapshot;
import com.tastyhouse.application.order.port.out.write.OrderState;
import com.tastyhouse.domain.coupon.vo.MemberCouponId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.vo.OrderDeliveryDestination;
import com.tastyhouse.domain.order.vo.OrderSchedule;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.vo.ShopId;

final class OrderStateMapper {
    private OrderStateMapper() {
    }

    static Order toDomain(OrderState state) {
        return Order.reconstitute(
            state.id(),
            state.memberId() == null ? null : MemberId.of(state.memberId()),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.orderNumber(),
            state.orderMethod() == null ? null : OrderMethod.valueOf(state.orderMethod()),
            state.orderStatus() == null ? null : OrderStatus.valueOf(state.orderStatus()),
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
            toDeliveryDestination(state.deliveryDestination()),
            toSchedule(state.schedule()),
            state.memberCouponId() == null ? null : MemberCouponId.of(state.memberCouponId()),
            state.usedPoint(),
            state.earnedPoint(),
            state.deleted(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static OrderState toState(Order order) {
        return new OrderState(
            order.getId(),
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
            toSnapshot(order.getDeliveryDestination()),
            toSnapshot(order.getSchedule()),
            order.getMemberCouponId() == null ? null : order.getMemberCouponId().value(),
            order.getUsedPoint(),
            order.getEarnedPoint(),
            order.isDeleted(),
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }

    private static OrderDeliveryDestination toDeliveryDestination(OrderDeliveryDestinationSnapshot snapshot) {
        return snapshot == null
            ? null
            : new OrderDeliveryDestination(
                snapshot.adminDongId(),
                snapshot.detailAddress(),
                snapshot.distanceMeters(),
                snapshot.latitude(),
                snapshot.longitude(),
                snapshot.lotAddress(),
                snapshot.roadAddress()
            );
    }

    private static OrderDeliveryDestinationSnapshot toSnapshot(OrderDeliveryDestination destination) {
        return destination == null
            ? null
            : new OrderDeliveryDestinationSnapshot(
                destination.adminDongId(),
                destination.detailAddress(),
                destination.distanceMeters(),
                destination.latitude(),
                destination.longitude(),
                destination.lotAddress(),
                destination.roadAddress()
            );
    }

    private static OrderSchedule toSchedule(OrderScheduleSnapshot snapshot) {
        return snapshot == null ? null : new OrderSchedule(snapshot.scheduledAt(), snapshot.scheduledSlotEndAt());
    }

    private static OrderScheduleSnapshot toSnapshot(OrderSchedule schedule) {
        return schedule == null ? null : new OrderScheduleSnapshot(schedule.scheduledAt(), schedule.scheduledSlotEndAt());
    }
}
