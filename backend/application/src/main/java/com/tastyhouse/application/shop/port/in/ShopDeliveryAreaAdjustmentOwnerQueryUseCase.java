package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentListItemResult;

@CeoApp
public interface ShopDeliveryAreaAdjustmentOwnerQueryUseCase {

    List<ShopDeliveryAreaAdjustmentListItemResult> getAdjustmentRequests(Long ceoId, Long shopId);
}
