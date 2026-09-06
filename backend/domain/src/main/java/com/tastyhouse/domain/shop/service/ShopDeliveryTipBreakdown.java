package com.tastyhouse.domain.shop.service;

public record ShopDeliveryTipBreakdown(
    int baseTipAmount,
    int distanceTipAmount,
    int regionTipAmount,
    int scheduleTipAmount,
    int holidayTipAmount,
    int totalTipAmount
) {
    public static ShopDeliveryTipBreakdown none() {
        return new ShopDeliveryTipBreakdown(0, 0, 0, 0, 0, 0);
    }

    public static ShopDeliveryTipBreakdown of(
        int baseTipAmount,
        int distanceTipAmount,
        int regionTipAmount,
        int scheduleTipAmount,
        int holidayTipAmount
    ) {
        return new ShopDeliveryTipBreakdown(
            baseTipAmount,
            distanceTipAmount,
            regionTipAmount,
            scheduleTipAmount,
            holidayTipAmount,
            baseTipAmount + distanceTipAmount + regionTipAmount + scheduleTipAmount + holidayTipAmount
        );
    }
}
