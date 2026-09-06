package com.tastyhouse.application.shop.port.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShopRiderGuideResult(
    Long shopId,
    String shopName,
    String visitGuide,
    String pickupRoadAddress,
    String pickupLotAddress,
    String pickupDetailAddress,
    BigDecimal pickupLatitude,
    BigDecimal pickupLongitude,
    String shopRoadAddress,
    String shopLotAddress,
    BigDecimal shopLatitude,
    BigDecimal shopLongitude,
    LocalDateTime updatedAt
) {
}
