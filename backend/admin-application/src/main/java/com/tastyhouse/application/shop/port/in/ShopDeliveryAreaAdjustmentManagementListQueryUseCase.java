package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentListItemResult;

public interface ShopDeliveryAreaAdjustmentManagementListQueryUseCase {

    PageResult<ShopDeliveryAreaAdjustmentListItemResult> getAdjustmentRequests(
        String status,
        Long shopId,
        int page,
        int size
    );
}
