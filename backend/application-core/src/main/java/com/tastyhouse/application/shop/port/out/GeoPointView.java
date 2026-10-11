package com.tastyhouse.application.shop.port.out;

import java.math.BigDecimal;

public record GeoPointView(
    BigDecimal latitude,
    BigDecimal longitude
) {
}
