package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopDeliveryAreaPolygonSaveCommand(
    Long ceoId,
    Long shopId,
    List<List<GeoPointCommand>> rings
) {

    public ShopDeliveryAreaPolygonSaveCommand {
        if (ceoId == null || shopId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
