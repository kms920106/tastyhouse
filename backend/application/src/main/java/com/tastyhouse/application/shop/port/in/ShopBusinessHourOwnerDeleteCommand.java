package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopBusinessHourOwnerDeleteCommand(
    Long ceoId,
    Long businessHourId
) {
    public ShopBusinessHourOwnerDeleteCommand {
        if (ceoId == null || businessHourId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopBusinessHourOwnerDeleteCommand of(Long ceoId, Long businessHourId) {
        return new ShopBusinessHourOwnerDeleteCommand(ceoId, businessHourId);
    }
}
