package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopDeliveryAreaPolygonSaveCommand(
    Long ceoId,
    Long shopId,
    List<List<GeoPointCommand>> rings
) {
    public ShopDeliveryAreaPolygonSaveCommand {
        if (ceoId == null || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
