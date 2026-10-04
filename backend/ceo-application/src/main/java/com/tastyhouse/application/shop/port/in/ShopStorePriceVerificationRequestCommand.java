package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopStorePriceVerificationRequestCommand(
    Long ceoId,
    Long shopId,
    String items
) {

    public ShopStorePriceVerificationRequestCommand {
        if (ceoId == null || shopId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
