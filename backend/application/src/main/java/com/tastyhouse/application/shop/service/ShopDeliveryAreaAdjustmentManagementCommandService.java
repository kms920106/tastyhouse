package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaAdjustmentManagementCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaAdjustmentRejectCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaAdjustmentStatusChangeCommand;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaAdjustmentService;

@Service
@AdminApp
@Transactional
public class ShopDeliveryAreaAdjustmentManagementCommandService implements ShopDeliveryAreaAdjustmentManagementCommandUseCase {

    private final ShopDeliveryAreaAdjustmentService shopDeliveryAreaAdjustmentService;

    public ShopDeliveryAreaAdjustmentManagementCommandService(ShopDeliveryAreaAdjustmentService shopDeliveryAreaAdjustmentService) {
        this.shopDeliveryAreaAdjustmentService = shopDeliveryAreaAdjustmentService;
    }

    @Override
    public void changeStatus(ShopDeliveryAreaAdjustmentStatusChangeCommand command) {
        Long requestId = command.requestId();
        String status = command.status();
        DeliveryAreaAdjustmentStatus targetStatus = DeliveryAreaAdjustmentStatus.from(status);

        switch (targetStatus) {
            case IN_PROGRESS -> shopDeliveryAreaAdjustmentService.startProgress(requestId);
            case COMPLETED -> shopDeliveryAreaAdjustmentService.complete(requestId);
            default -> throw new BusinessException(ErrorCode.DELIVERY_AREA_ADJUSTMENT_STATUS_UNKNOWN,
                ErrorCode.DELIVERY_AREA_ADJUSTMENT_STATUS_UNKNOWN.getDefaultMessage() + ": " + status);
        }
    }

    @Override
    public void rejectAdjustment(ShopDeliveryAreaAdjustmentRejectCommand command) {
        Long requestId = command.requestId();
        String reason = command.reason();
        shopDeliveryAreaAdjustmentService.reject(requestId, reason);
    }
}
