package com.tastyhouse.core.domain.order.application.dto.command;

import java.util.List;

import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;

public record OrderCreateCommand(
    Long shopId,
    OrderMethod orderMethod,
    List<OrderProductCreateCommand> orderProducts,
    Long memberCouponId,
    Integer usePoint,
    Integer totalProductAmount,
    Integer totalDiscountAmount,
    Integer productDiscountAmount,
    Integer couponDiscountAmount,
    Integer finalAmount
) {

    public static OrderCreateCommand of(
        Long shopId,
        OrderMethod orderMethod,
        List<OrderProductCreateCommand> orderProducts,
        Long memberCouponId,
        Integer usePoint,
        Integer totalProductAmount,
        Integer totalDiscountAmount,
        Integer productDiscountAmount,
        Integer couponDiscountAmount,
        Integer finalAmount
    ) {
        return new OrderCreateCommand(
            shopId,
            orderMethod,
            orderProducts,
            memberCouponId,
            usePoint,
            totalProductAmount,
            totalDiscountAmount,
            productDiscountAmount,
            couponDiscountAmount,
            finalAmount
        );
    }
}
