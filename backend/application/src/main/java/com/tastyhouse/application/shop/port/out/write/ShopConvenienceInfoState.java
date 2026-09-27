package com.tastyhouse.application.shop.port.out.write;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShopConvenienceInfoState(
    Long id,
    Long shopId,
    boolean parkingAvailable,
    boolean parkingPaid,
    boolean valetAvailable,
    boolean valetPaid,
    String directionsGuide,
    BigDecimal displayLatitude,
    BigDecimal displayLongitude,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
