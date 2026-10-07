package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaAdjustmentRejectCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaAdjustmentRejectUseCase;

@Service
@Transactional
class ShopDeliveryAreaAdjustmentRejectService implements ShopDeliveryAreaAdjustmentRejectUseCase {

    private final ShopDeliveryAreaAdjustmentService shopDeliveryAreaAdjustmentService;

    public ShopDeliveryAreaAdjustmentRejectService(ShopDeliveryAreaAdjustmentService shopDeliveryAreaAdjustmentService) {
        this.shopDeliveryAreaAdjustmentService = shopDeliveryAreaAdjustmentService;
    }

    @Override
    public void rejectAdjustment(ShopDeliveryAreaAdjustmentRejectCommand command) {
        Long requestId = command.requestId();
        String reason = command.reason();
        shopDeliveryAreaAdjustmentService.reject(requestId, reason);
    }
}
