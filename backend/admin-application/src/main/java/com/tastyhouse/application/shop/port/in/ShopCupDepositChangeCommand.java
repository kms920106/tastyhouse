package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopCupDepositChangeCommand(
    Long shopId,
    Boolean enabled
) {

    public ShopCupDepositChangeCommand {
        if (shopId == null || enabled == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
