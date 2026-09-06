package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.application.shop.port.out.ShopRiderGuideListItemResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShopRiderGuidePickupPresenceResult(
    Long shopId,
    String shopName,
    String visitGuide,
    String pickupRoadAddress,
    BigDecimal pickupLatitude,
    BigDecimal pickupLongitude,
    LocalDateTime updatedAt
) {
    public ShopRiderGuideListItemResult toListItem() {
        boolean hasPickupLocation = pickupRoadAddress != null && pickupLatitude != null && pickupLongitude != null;

        return new ShopRiderGuideListItemResult(
            shopId,
            shopName,
            visitGuide,
            hasPickupLocation,
            updatedAt
        );
    }
}
