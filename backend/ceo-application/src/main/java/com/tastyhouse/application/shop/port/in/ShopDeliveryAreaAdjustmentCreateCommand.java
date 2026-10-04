package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopDeliveryAreaAdjustmentCreateCommand(
    Long ceoId,
    Long shopId,
    String counterpartShopName,
    String counterpartBusinessNumber,
    String franchiseName,
    String reason
) {

    public ShopDeliveryAreaAdjustmentCreateCommand {
        if (ceoId == null || shopId == null || counterpartShopName == null
            || counterpartBusinessNumber == null || franchiseName == null || reason == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
