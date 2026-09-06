package com.tastyhouse.application.shop.port.out;

import java.util.List;

public record ShopBestListItemViewResult(
    Long id,
    String name,
    String stationName,
    Double rating,
    String imageUrl,
    List<String> foodTypes,
    String operatingStatus,
    int minOrderAmount,
    int minDeliveryTip,
    int maxDeliveryTip
) {
}
