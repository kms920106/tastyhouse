package com.tastyhouse.application.shop.port.out;

import java.math.BigDecimal;

public record ShopVisibleDetailResult(
    Long id,
    String name,
    BigDecimal latitude,
    BigDecimal longitude,
    Double rating,
    String roadAddress,
    String lotAddress,
    String phoneNumber,
    int minOrderAmount,
    boolean scheduledOrderEnabled
) {
}
