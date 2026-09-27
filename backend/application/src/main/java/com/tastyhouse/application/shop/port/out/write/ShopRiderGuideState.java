package com.tastyhouse.application.shop.port.out.write;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShopRiderGuideState(
    Long id,
    Long shopId,
    String visitGuide,
    String pickupRoadAddress,
    String pickupLotAddress,
    String pickupDetailAddress,
    BigDecimal pickupLatitude,
    BigDecimal pickupLongitude,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
