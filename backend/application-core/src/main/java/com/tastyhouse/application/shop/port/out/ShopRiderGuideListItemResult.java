package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

public record ShopRiderGuideListItemResult(
    Long shopId,
    String shopName,
    String visitGuide,
    boolean hasPickupLocation,
    LocalDateTime updatedAt
) {
}
