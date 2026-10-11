package com.tastyhouse.application.shop.port.out;

import java.math.BigDecimal;

public record ShopOwnerDetailViewResult(
    Long id,
    Long stationId,
    String name,
    BigDecimal latitude,
    BigDecimal longitude,
    Double rating,
    String roadAddress,
    String lotAddress,
    String phoneNumber,
    String thumbnailImageUrl,
    String trademarkImageUrl,
    boolean permanentlyClosed,
    boolean hidden,
    boolean closedOnPublicHolidays,
    int minOrderAmount,
    boolean scheduledOrderEnabled,
    boolean cupDepositEnabled
) {
}
