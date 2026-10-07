package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaAdjustmentStatusChangeCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaAdjustmentStatusChangeUseCase;

@Service
@Transactional
class ShopDeliveryAreaAdjustmentStatusChangeService implements ShopDeliveryAreaAdjustmentStatusChangeUseCase {

    private final ShopDeliveryAreaAdjustmentService shopDeliveryAreaAdjustmentService;

    public ShopDeliveryAreaAdjustmentStatusChangeService(ShopDeliveryAreaAdjustmentService shopDeliveryAreaAdjustmentService) {
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
            default -> throw new DomainException(DomainErrorCode.DELIVERY_AREA_ADJUSTMENT_STATUS_UNKNOWN,
                DomainErrorCode.DELIVERY_AREA_ADJUSTMENT_STATUS_UNKNOWN.getDefaultMessage() + ": " + status);
        }
    }
}
