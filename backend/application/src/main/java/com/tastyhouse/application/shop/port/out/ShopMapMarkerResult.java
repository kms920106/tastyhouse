package com.tastyhouse.application.shop.port.out;

import java.math.BigDecimal;

public record ShopMapMarkerResult(
    Long id,
    BigDecimal latitude,
    BigDecimal longitude,
    String name
) {
}
