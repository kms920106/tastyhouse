package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaAdjustmentManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentDetailResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentListItemResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentManagementQueryPort;

@Service
@Transactional(readOnly = true)
class ShopDeliveryAreaAdjustmentManagementQueryService implements ShopDeliveryAreaAdjustmentManagementQueryUseCase {

    private final ShopDeliveryAreaAdjustmentManagementQueryPort shopDeliveryAreaAdjustmentManagementQueryPort;

    public ShopDeliveryAreaAdjustmentManagementQueryService(ShopDeliveryAreaAdjustmentManagementQueryPort shopDeliveryAreaAdjustmentManagementQueryPort) {
        this.shopDeliveryAreaAdjustmentManagementQueryPort = shopDeliveryAreaAdjustmentManagementQueryPort;
    }

    @Override
    public PageResult<ShopDeliveryAreaAdjustmentListItemResult> getAdjustmentRequests(
        String status,
        Long shopId,
        int page,
        int size
    ) {
        String adjustmentStatus = status == null ? null : DeliveryAreaAdjustmentStatus.from(status).name();

        return shopDeliveryAreaAdjustmentManagementQueryPort
            .findAdjustmentRequestPage(adjustmentStatus, shopId, PageQuery.of(page, size));
    }

    @Override
    public ShopDeliveryAreaAdjustmentDetailResult getAdjustmentRequest(Long requestId) {
        return shopDeliveryAreaAdjustmentManagementQueryPort.findAdjustmentRequestById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_DELIVERY_AREA_ADJUSTMENT_REQUEST_NOT_FOUND));
    }
}
