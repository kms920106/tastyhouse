package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopMinOrderAmountUpdateCommand(
    Long ceoId,
    Long shopId,
    Integer minOrderAmount
) {

    public ShopMinOrderAmountUpdateCommand {
        if (ceoId == null || shopId == null || minOrderAmount == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
