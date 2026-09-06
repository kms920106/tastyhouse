package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentDetailResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentListItemResult;
import com.tastyhouse.domain.shared.page.PageResult;

@AdminApp
public interface ShopDeliveryAreaAdjustmentManagementQueryUseCase {

    PageResult<ShopDeliveryAreaAdjustmentListItemResult> getAdjustmentRequests(
        String status,
        Long shopId,
        int page,
        int size
    );

    ShopDeliveryAreaAdjustmentDetailResult getAdjustmentRequest(Long requestId);
}
