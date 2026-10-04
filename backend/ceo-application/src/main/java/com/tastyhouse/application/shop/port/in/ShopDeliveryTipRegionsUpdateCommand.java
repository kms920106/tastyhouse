package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopDeliveryTipRegionsUpdateCommand(
    Long ceoId,
    Long shopId,
    List<ShopDeliveryTipRegionCommand> regions
) {

    public ShopDeliveryTipRegionsUpdateCommand {
        if (ceoId == null || shopId == null || regions == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
