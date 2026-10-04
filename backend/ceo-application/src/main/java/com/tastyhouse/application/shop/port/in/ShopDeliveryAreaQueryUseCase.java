package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaItemResult;

public interface ShopDeliveryAreaQueryUseCase {

    List<ShopDeliveryAreaItemResult> getDeliveryAreas(Long ceoId, Long shopId);
}
