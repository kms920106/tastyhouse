package com.tastyhouse.application.shop.port.out;

import java.math.BigDecimal;
import java.util.List;

public record ShopDeliveryAreaRadiusPreviewResult(
    BigDecimal centerLatitude,
    BigDecimal centerLongitude,
    int radiusMeters,
    int maxAllowedRadiusMeters,
    int defaultExposureRadiusMeters,
    List<GeoPointView> circle,
    List<ShopDeliveryAreaCandidateView> adminDongs,
    int adminDongCount,
    int unresolvedCount
) {
}
