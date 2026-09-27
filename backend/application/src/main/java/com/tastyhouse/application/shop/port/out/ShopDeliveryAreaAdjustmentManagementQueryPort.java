package com.tastyhouse.application.shop.port.out;

import java.util.Optional;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;

public interface ShopDeliveryAreaAdjustmentManagementQueryPort {

    PageResult<ShopDeliveryAreaAdjustmentListItemResult> findAdjustmentRequestPage(DeliveryAreaAdjustmentStatus status, Long shopId, PageQuery pageQuery);

    Optional<ShopDeliveryAreaAdjustmentDetailResult> findAdjustmentRequestById(Long requestId);
}
