package com.tastyhouse.application.shop.port.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ShopDeliveryAreaPolygonViewResult(
    boolean exists,
    List<List<GeoPointView>> rings,
    BigDecimal centerLatitude,
    BigDecimal centerLongitude,
    BigDecimal shopLatitude,
    BigDecimal shopLongitude,
    int centerMovedMeters,
    Integer maxRadiusMeters,
    int maxAllowedRadiusMeters,
    int defaultExposureRadiusMeters,
    Integer ringCount,
    Integer vertexCount,
    int projectedAdminDongCount,
    LocalDateTime updatedAt
) {
}
