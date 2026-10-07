package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaAdjustmentManagementDetailQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentDetailResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaAdjustmentManagementQueryPort;

@Service
@Transactional(readOnly = true)
class ShopDeliveryAreaAdjustmentManagementDetailQueryService implements ShopDeliveryAreaAdjustmentManagementDetailQueryUseCase {

    private final ShopDeliveryAreaAdjustmentManagementQueryPort shopDeliveryAreaAdjustmentManagementQueryPort;

    public ShopDeliveryAreaAdjustmentManagementDetailQueryService(ShopDeliveryAreaAdjustmentManagementQueryPort shopDeliveryAreaAdjustmentManagementQueryPort) {
        this.shopDeliveryAreaAdjustmentManagementQueryPort = shopDeliveryAreaAdjustmentManagementQueryPort;
    }

    @Override
    public ShopDeliveryAreaAdjustmentDetailResult getAdjustmentRequest(Long requestId) {
        return shopDeliveryAreaAdjustmentManagementQueryPort.findAdjustmentRequestById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_DELIVERY_AREA_ADJUSTMENT_REQUEST_NOT_FOUND));
    }
}
