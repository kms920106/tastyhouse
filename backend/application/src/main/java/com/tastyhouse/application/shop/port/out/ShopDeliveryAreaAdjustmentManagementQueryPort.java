package com.tastyhouse.application.shop.port.out;

import java.util.Optional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;

public interface ShopDeliveryAreaAdjustmentManagementQueryPort {

    PageResult<ShopDeliveryAreaAdjustmentListItemResult> findAdjustmentRequestPage(DeliveryAreaAdjustmentStatus status, Long shopId, PageQuery pageQuery);

    Optional<ShopDeliveryAreaAdjustmentDetailResult> findAdjustmentRequestById(Long requestId);
}
