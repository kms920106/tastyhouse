package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentDetailResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentListItemResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentManagementQueryPort;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaAdjustmentManagementQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class ShopDeliveryAreaAdjustmentManagementQueryService implements ShopDeliveryAreaAdjustmentManagementQueryUseCase {

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
        DeliveryAreaAdjustmentStatus adjustmentStatus = status == null ? null : DeliveryAreaAdjustmentStatus.from(status);

        return shopDeliveryAreaAdjustmentManagementQueryPort
            .findAdjustmentRequestPage(adjustmentStatus, shopId, PageQuery.of(page, size));
    }

    @Override
    public ShopDeliveryAreaAdjustmentDetailResult getAdjustmentRequest(Long requestId) {
        return shopDeliveryAreaAdjustmentManagementQueryPort.findAdjustmentRequestById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_DELIVERY_AREA_ADJUSTMENT_REQUEST_NOT_FOUND));
    }
}
