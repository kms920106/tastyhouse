package com.tastyhouse.application.shop.port.out;

import java.math.BigDecimal;

public record ShopLocationResult(
    long shopId,
    BigDecimal latitude,
    BigDecimal longitude
) {
}
