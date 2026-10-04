package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopDeliveryAreaRadiusApplyCommand(
    Long ceoId,
    Long shopId,
    Integer radiusMeters,
    Boolean replace
) {

    public ShopDeliveryAreaRadiusApplyCommand {
        if (ceoId == null || shopId == null || radiusMeters == null || replace == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
