package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopDeliveryTipDistanceRemoveCommand(
    Long ceoId,
    Long shopId
) {

    public ShopDeliveryTipDistanceRemoveCommand {
        if (ceoId == null || shopId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopDeliveryTipDistanceRemoveCommand of(Long ceoId, Long shopId) {
        return new ShopDeliveryTipDistanceRemoveCommand(ceoId, shopId);
    }
}
