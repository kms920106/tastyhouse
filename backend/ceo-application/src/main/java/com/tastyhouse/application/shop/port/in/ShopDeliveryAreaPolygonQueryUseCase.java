package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaPolygonPreviewResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaPolygonViewResult;

public interface ShopDeliveryAreaPolygonQueryUseCase {

    ShopDeliveryAreaPolygonViewResult getPolygon(Long ceoId, Long shopId);

    ShopDeliveryAreaPolygonPreviewResult previewPolygon(Long ceoId, Long shopId, List<List<GeoPointCommand>> rings);
}
