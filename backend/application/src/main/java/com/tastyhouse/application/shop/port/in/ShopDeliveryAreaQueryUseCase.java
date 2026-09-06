package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaItemResult;

@CeoApp
public interface ShopDeliveryAreaQueryUseCase {

    List<ShopDeliveryAreaItemResult> getDeliveryAreas(Long ceoId, Long shopId);
}
