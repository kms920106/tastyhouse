package com.tastyhouse.application.shop.port.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShopDeliveryAreaPolygonResult(
    long id,
    String rings,
    BigDecimal centerLatitude,
    BigDecimal centerLongitude,
    Integer maxRadiusMeters,
    Integer ringCount,
    Integer vertexCount,
    LocalDateTime updatedAt
) {
}
