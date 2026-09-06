package com.tastyhouse.domain.order.service;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.shared.model.OrderMethod;

public record OrderPlacement(
    Long shopId,
    OrderMethod orderMethod,
    List<OrderPlacementItem> items,
    Long memberCouponId,
    Integer usePoint,
    Long deliveryAddressId,
    Integer totalProductAmount,
    Integer totalDiscountAmount,
    Integer productDiscountAmount,
    Integer couponDiscountAmount,
    Integer deliveryTipAmount,
    Integer cupDepositAmount,
    Integer finalAmount,
    LocalDateTime scheduledAt
) {
    public static OrderPlacement of(
        Long shopId,
        OrderMethod orderMethod,
        List<OrderPlacementItem> items,
        Long memberCouponId,
        Integer usePoint,
        Long deliveryAddressId,
        Integer totalProductAmount,
        Integer totalDiscountAmount,
        Integer productDiscountAmount,
        Integer couponDiscountAmount,
        Integer deliveryTipAmount,
        Integer cupDepositAmount,
        Integer finalAmount,
        LocalDateTime scheduledAt
    ) {
        return new OrderPlacement(
            shopId,
            orderMethod,
            items,
            memberCouponId,
            usePoint,
            deliveryAddressId,
            totalProductAmount,
            totalDiscountAmount,
            productDiscountAmount,
            couponDiscountAmount,
            deliveryTipAmount,
            cupDepositAmount,
            finalAmount,
            scheduledAt
        );
    }
}
