package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopDeliveryTipRegionsRemoveCommand(
    Long ceoId,
    Long shopId
) {

    public ShopDeliveryTipRegionsRemoveCommand {
        if (ceoId == null || shopId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopDeliveryTipRegionsRemoveCommand of(Long ceoId, Long shopId) {
        return new ShopDeliveryTipRegionsRemoveCommand(ceoId, shopId);
    }
}
