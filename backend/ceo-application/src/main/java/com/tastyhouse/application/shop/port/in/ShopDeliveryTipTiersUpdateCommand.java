package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopDeliveryTipTiersUpdateCommand(
    Long ceoId,
    Long shopId,
    List<ShopDeliveryTipTierCommand> tiers
) {

    public ShopDeliveryTipTiersUpdateCommand {
        if (ceoId == null || shopId == null || tiers == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
