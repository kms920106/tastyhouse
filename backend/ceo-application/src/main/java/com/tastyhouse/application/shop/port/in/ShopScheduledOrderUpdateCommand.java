package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopScheduledOrderUpdateCommand(
    Long ceoId,
    Long shopId,
    Boolean enabled
) {

    public ShopScheduledOrderUpdateCommand {
        if (ceoId == null || shopId == null || enabled == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
