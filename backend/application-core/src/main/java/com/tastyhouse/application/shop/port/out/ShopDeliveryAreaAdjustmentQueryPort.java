package com.tastyhouse.application.shop.port.out;

import java.util.List;

public interface ShopDeliveryAreaAdjustmentQueryPort {

    List<ShopDeliveryAreaAdjustmentListItemResult> findAdjustmentRequests(Long shopId);
}
