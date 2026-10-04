package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopDeliveryAreaAdjustmentRejectCommand(
    Long requestId,
    String reason
) {

    public ShopDeliveryAreaAdjustmentRejectCommand {
        if (requestId == null || reason == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
