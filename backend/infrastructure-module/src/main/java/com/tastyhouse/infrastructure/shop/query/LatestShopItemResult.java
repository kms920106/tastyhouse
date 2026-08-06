package com.tastyhouse.infrastructure.shop.query;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.shop.model.FoodType;

public record LatestShopItemResult(
    Long id,
    String name,
    String stationName,
    Double rating,
    String imageUrl,
    LocalDateTime createdAt,
    Long reviewCount,
    Long bookmarkCount,
    List<FoodType> foodTypes,
    int minOrderAmount,
    int minDeliveryTip,
    int maxDeliveryTip
) {
}
