package com.tastyhouse.core.domain.shop.application.dto.result;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.core.domain.shop.domain.model.FoodType;

public record LatestShopItemResult(
    Long id,
    String name,
    String stationName,
    Double rating,
    String imageUrl,
    LocalDateTime createdAt,
    Long reviewCount,
    Long bookmarkCount,
    List<FoodType> foodTypes
) {
}
