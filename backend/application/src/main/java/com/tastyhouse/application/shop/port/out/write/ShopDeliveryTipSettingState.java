package com.tastyhouse.application.shop.port.out.write;

import java.time.LocalDateTime;

public record ShopDeliveryTipSettingState(
    Long id,
    Long shopId,
    String extraTipType,
    Integer baseDistanceMeters,
    String surchargeUnit,
    Integer surchargeAmount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
