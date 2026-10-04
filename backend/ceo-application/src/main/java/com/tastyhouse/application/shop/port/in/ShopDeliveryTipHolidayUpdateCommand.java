package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopDeliveryTipHolidayUpdateCommand(
    Long ceoId,
    Long shopId,
    Integer tipAmount
) {

    public ShopDeliveryTipHolidayUpdateCommand {
        if (ceoId == null || shopId == null || tipAmount == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
