package com.tastyhouse.application.shop.port.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShopManagementDetailResult(
    Long id,
    Long stationId,
    String name,
    BigDecimal latitude,
    BigDecimal longitude,
    Double rating,
    String roadAddress,
    String lotAddress,
    String phoneNumber,
    boolean permanentlyClosed,
    boolean cupDepositEnabled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
