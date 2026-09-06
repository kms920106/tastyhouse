package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaPolygonPreviewResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaPolygonViewResult;

@CeoApp
public interface ShopDeliveryAreaPolygonQueryUseCase {

    ShopDeliveryAreaPolygonViewResult getPolygon(Long ceoId, Long shopId);

    ShopDeliveryAreaPolygonPreviewResult previewPolygon(Long ceoId, Long shopId, List<List<GeoPointCommand>> rings);
}
