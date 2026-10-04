package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopDeliveryAreaAdjustmentStatusChangeCommand(
    Long requestId,
    String status
) {

    public ShopDeliveryAreaAdjustmentStatusChangeCommand {
        if (requestId == null || status == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
