package com.tastyhouse.application.shop.port.out.write;

public record ShopDeliveryAreaPolygonState(
    Long id,
    Long shopId,
    ShopDeliveryAreaPolygonShapeSnapshot polygon,
    ShopDeliveryAreaPolygonCenterSnapshot center,
    int maxRadiusMeters
) {
}
