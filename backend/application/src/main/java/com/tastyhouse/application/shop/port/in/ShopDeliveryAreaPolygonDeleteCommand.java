package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopDeliveryAreaPolygonDeleteCommand(
    Long ceoId,
    Long shopId
) {
    public ShopDeliveryAreaPolygonDeleteCommand {
        if (ceoId == null || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopDeliveryAreaPolygonDeleteCommand of(Long ceoId, Long shopId) {
        return new ShopDeliveryAreaPolygonDeleteCommand(ceoId, shopId);
    }
}
