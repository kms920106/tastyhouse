package com.tastyhouse.core.domain.order.application.dto.command;

import java.util.List;

import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;

public record CreateOrderCommand(
    Long shopId,
    OrderMethod orderMethod,
    List<CreateOrderProductCommand> orderProducts,
    Long memberCouponId,
    Integer usePoint,
    Integer totalProductAmount,
    Integer totalDiscountAmount,
    Integer productDiscountAmount,
    Integer couponDiscountAmount,
    Integer finalAmount
) {
}
