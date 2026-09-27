package com.tastyhouse.application.shop.port.out.write;

import java.math.BigDecimal;

public record ShopDeliveryAreaPolygonCenterSnapshot(
    BigDecimal latitude,
    BigDecimal longitude
) {
}
