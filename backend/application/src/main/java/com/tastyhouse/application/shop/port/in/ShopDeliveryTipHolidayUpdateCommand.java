package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopDeliveryTipHolidayUpdateCommand(
    Long ceoId,
    Long shopId,
    Integer tipAmount
) {
    public ShopDeliveryTipHolidayUpdateCommand {
        if (ceoId == null || shopId == null || tipAmount == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
