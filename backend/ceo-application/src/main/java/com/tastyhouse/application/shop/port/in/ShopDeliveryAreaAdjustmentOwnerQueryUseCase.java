package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentListItemResult;

public interface ShopDeliveryAreaAdjustmentOwnerQueryUseCase {

    List<ShopDeliveryAreaAdjustmentListItemResult> getAdjustmentRequests(Long ceoId, Long shopId);
}
