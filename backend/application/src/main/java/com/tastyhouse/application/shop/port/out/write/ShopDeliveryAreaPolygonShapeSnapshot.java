package com.tastyhouse.application.shop.port.out.write;

public record ShopDeliveryAreaPolygonShapeSnapshot(
    String encodedRings,
    int ringCount,
    int vertexCount
) {
}
