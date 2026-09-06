package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopDeliveryAreaAdjustmentStatusChangeCommand(
    Long requestId,
    String status
) {
    public ShopDeliveryAreaAdjustmentStatusChangeCommand {
        if (requestId == null || status == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
