package com.tastyhouse.application.order.port.out.write;

import java.time.LocalDateTime;

public record OrderState(
    Long id,
    Long memberId,
    Long shopId,
    String orderNumber,
    String orderMethod,
    String orderStatus,
    String ordererName,
    String ordererPhone,
    String ordererEmail,
    Integer totalProductAmount,
    Integer productDiscountAmount,
    Integer couponDiscountAmount,
    Integer pointDiscountAmount,
    Integer totalDiscountAmount,
    Integer deliveryTipAmount,
    Integer cupDepositAmount,
    Integer finalAmount,
    OrderDeliveryDestinationSnapshot deliveryDestination,
    OrderScheduleSnapshot schedule,
    Long memberCouponId,
    Integer usedPoint,
    Integer earnedPoint,
    boolean deleted,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
