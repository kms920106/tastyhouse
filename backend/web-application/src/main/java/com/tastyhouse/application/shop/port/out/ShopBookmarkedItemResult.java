package com.tastyhouse.application.shop.port.out;

public record ShopBookmarkedItemResult(
    Long shopId,
    Long bookmarkId,
    String shopName,
    String stationName,
    Double rating,
    String imageUrl,
    boolean bookmarked,
    int minOrderAmount,
    int minDeliveryTip,
    int maxDeliveryTip
) {
}
