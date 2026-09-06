package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;
import java.util.List;

public record ShopLatestListItemViewResult(
    Long id,
    String name,
    String stationName,
    Double rating,
    String imageUrl,
    LocalDateTime createdAt,
    Long reviewCount,
    Long bookmarkCount,
    List<String> foodTypes,
    String operatingStatus,
    int minOrderAmount,
    int minDeliveryTip,
    int maxDeliveryTip
) {
}
