package com.tastyhouse.application.shop.port.out;

import java.util.List;

public record BestShopItemResult(
    Long id,
    String name,
    String stationName,
    Double rating,
    String imageUrl,
    List<String> foodTypes,
    int minOrderAmount,
    int minDeliveryTip,
    int maxDeliveryTip
) {
}
