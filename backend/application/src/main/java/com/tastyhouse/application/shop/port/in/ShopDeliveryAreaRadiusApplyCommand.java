package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopDeliveryAreaRadiusApplyCommand(
    Long ceoId,
    Long shopId,
    Integer radiusMeters,
    Boolean replace
) {
    public ShopDeliveryAreaRadiusApplyCommand {
        if (ceoId == null || shopId == null || radiusMeters == null || replace == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
