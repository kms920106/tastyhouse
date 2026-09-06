package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopDeliveryAreaAdjustmentRejectCommand(
    Long requestId,
    String reason
) {
    public ShopDeliveryAreaAdjustmentRejectCommand {
        if (requestId == null || reason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
