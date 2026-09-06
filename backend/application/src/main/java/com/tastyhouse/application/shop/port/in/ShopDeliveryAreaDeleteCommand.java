package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopDeliveryAreaDeleteCommand(
    Long ceoId,
    Long deliveryAreaId
) {
    public ShopDeliveryAreaDeleteCommand {
        if (ceoId == null || deliveryAreaId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopDeliveryAreaDeleteCommand of(Long ceoId, Long deliveryAreaId) {
        return new ShopDeliveryAreaDeleteCommand(ceoId, deliveryAreaId);
    }
}
