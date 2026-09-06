package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaRadiusPreviewResult;

@CeoApp
public interface ShopDeliveryAreaRadiusQueryUseCase {

    ShopDeliveryAreaRadiusPreviewResult previewRadius(Long ceoId, Long shopId, int radiusMeters);
}
