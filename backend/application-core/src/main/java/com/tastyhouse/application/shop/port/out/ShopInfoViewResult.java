package com.tastyhouse.application.shop.port.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ShopInfoViewResult(
    List<ShopClosedDayResult> closedDays,
    List<ShopBusinessHourResult> businessHours,
    List<ShopBreakTimeResult> breakTimes,
    List<ShopAmenityWithCategoryResult> amenities,
    String ownerMessage,
    LocalDateTime ownerMessageCreatedAt,
    Boolean parkingAvailable,
    Boolean parkingPaid,
    Boolean valetAvailable,
    Boolean valetPaid,
    String directionsGuide,
    BigDecimal displayLatitude,
    BigDecimal displayLongitude
) {
}
