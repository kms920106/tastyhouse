package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopDeliveryAreaPolygonDeleteCommand(
    Long ceoId,
    Long shopId
) {

    public ShopDeliveryAreaPolygonDeleteCommand {
        if (ceoId == null || shopId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopDeliveryAreaPolygonDeleteCommand of(Long ceoId, Long shopId) {
        return new ShopDeliveryAreaPolygonDeleteCommand(ceoId, shopId);
    }
}
