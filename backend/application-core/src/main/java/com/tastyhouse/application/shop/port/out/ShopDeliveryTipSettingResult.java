package com.tastyhouse.application.shop.port.out;

public record ShopDeliveryTipSettingResult(
    Long id,
    String extraTipType,
    Integer baseDistanceMeters,
    String surchargeUnit,
    Integer surchargeAmount
) {
}
