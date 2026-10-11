package com.tastyhouse.application.shop.port.out;

import java.util.Optional;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ShopDeliveryAreaAdjustmentManagementQueryPort {

    PageResult<ShopDeliveryAreaAdjustmentListItemResult> findAdjustmentRequestPage(String status, Long shopId, PageQuery pageQuery);

    Optional<ShopDeliveryAreaAdjustmentDetailResult> findAdjustmentRequestById(Long requestId);
}
