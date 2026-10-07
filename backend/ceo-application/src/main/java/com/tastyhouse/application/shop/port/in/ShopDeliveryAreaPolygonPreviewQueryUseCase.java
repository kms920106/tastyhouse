package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaPolygonPreviewResult;

public interface ShopDeliveryAreaPolygonPreviewQueryUseCase {

    ShopDeliveryAreaPolygonPreviewResult previewPolygon(Long ceoId, Long shopId, List<List<GeoPointCommand>> rings);
}
