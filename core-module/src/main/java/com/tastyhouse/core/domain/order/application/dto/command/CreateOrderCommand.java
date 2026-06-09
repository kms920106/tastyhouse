package com.tastyhouse.core.domain.order.application.dto.command;

import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;

import java.util.List;

public record CreateOrderCommand(
    Long shopId,
    OrderMethod orderMethod,
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
