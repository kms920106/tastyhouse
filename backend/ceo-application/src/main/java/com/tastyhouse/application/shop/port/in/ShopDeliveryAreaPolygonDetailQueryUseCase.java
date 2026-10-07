package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaPolygonViewResult;

public interface ShopDeliveryAreaPolygonDetailQueryUseCase {

    ShopDeliveryAreaPolygonViewResult getPolygon(Long ceoId, Long shopId);
}
