package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopMinOrderAmountUpdateCommand(
    Long ceoId,
    Long shopId,
    Integer minOrderAmount
) {
    public ShopMinOrderAmountUpdateCommand {
        if (ceoId == null || shopId == null || minOrderAmount == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
