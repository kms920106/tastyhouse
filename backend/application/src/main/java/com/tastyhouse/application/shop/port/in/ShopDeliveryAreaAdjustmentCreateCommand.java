package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

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
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
