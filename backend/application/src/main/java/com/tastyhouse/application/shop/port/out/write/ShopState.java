package com.tastyhouse.application.shop.port.out.write;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShopState(
    Long id,
    Long ceoId,
    Long stationId,
    String name,
    BigDecimal latitude,
    BigDecimal longitude,
    Double rating,
    String roadAddress,
    String lotAddress,
    String phoneNumber,
    Long thumbnailImageFileId,
    Long trademarkImageFileId,
    boolean permanentlyClosed,
    boolean hidden,
    boolean closedOnPublicHolidays,
    int minOrderAmount,
    boolean scheduledOrderEnabled,
    boolean cupDepositEnabled,
    boolean storePriceVerified,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
