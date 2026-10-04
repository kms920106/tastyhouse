package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopRiderPickupLocationClearCommand(
    Long ceoId,
    Long shopId
) {

    public ShopRiderPickupLocationClearCommand {
        if (ceoId == null || shopId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopRiderPickupLocationClearCommand of(Long ceoId, Long shopId) {
        return new ShopRiderPickupLocationClearCommand(ceoId, shopId);
    }
}
