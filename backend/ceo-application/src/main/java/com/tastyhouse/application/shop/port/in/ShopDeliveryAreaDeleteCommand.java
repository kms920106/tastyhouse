package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopDeliveryAreaDeleteCommand(
    Long ceoId,
    Long deliveryAreaId
) {

    public ShopDeliveryAreaDeleteCommand {
        if (ceoId == null || deliveryAreaId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopDeliveryAreaDeleteCommand of(Long ceoId, Long deliveryAreaId) {
        return new ShopDeliveryAreaDeleteCommand(ceoId, deliveryAreaId);
    }
}
