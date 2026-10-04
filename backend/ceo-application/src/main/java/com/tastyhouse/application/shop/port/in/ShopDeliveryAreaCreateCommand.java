package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopDeliveryAreaCreateCommand(
    Long ceoId,
    Long shopId,
    Long adminDongId
) {

    public ShopDeliveryAreaCreateCommand {
        if (ceoId == null || shopId == null || adminDongId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
