package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopDeliveryAreaAdjustmentRequest;

public interface ShopDeliveryAreaAdjustmentRequestSavePort {

    ShopDeliveryAreaAdjustmentRequest save(ShopDeliveryAreaAdjustmentRequest request);
}
