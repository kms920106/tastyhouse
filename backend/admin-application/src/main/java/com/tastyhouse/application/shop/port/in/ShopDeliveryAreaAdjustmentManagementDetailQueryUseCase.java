package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentDetailResult;

public interface ShopDeliveryAreaAdjustmentManagementDetailQueryUseCase {

    ShopDeliveryAreaAdjustmentDetailResult getAdjustmentRequest(Long requestId);
}
