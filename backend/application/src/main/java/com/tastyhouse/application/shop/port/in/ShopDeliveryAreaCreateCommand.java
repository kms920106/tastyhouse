package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopDeliveryAreaCreateCommand(
    Long ceoId,
    Long shopId,
    Long adminDongId
) {
    public ShopDeliveryAreaCreateCommand {
        if (ceoId == null || shopId == null || adminDongId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
