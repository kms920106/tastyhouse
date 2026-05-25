package com.tastyhouse.core.domain.order.application.dto.command;

import java.util.List;

public record CreateOrderCommand(
    Long placeId,
    List<CreateOrderItemCommand> orderItems,
    Long memberCouponId,
    Integer usePoint,
    Integer totalProductAmount,
    Integer totalDiscountAmount,
    Integer productDiscountAmount,
    Integer couponDiscountAmount,
    Integer finalAmount
) {
}
