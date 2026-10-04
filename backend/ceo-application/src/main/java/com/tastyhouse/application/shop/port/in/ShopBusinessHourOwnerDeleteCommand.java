package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopBusinessHourOwnerDeleteCommand(
    Long ceoId,
    Long businessHourId
) {

    public ShopBusinessHourOwnerDeleteCommand {
        if (ceoId == null || businessHourId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopBusinessHourOwnerDeleteCommand of(Long ceoId, Long businessHourId) {
        return new ShopBusinessHourOwnerDeleteCommand(ceoId, businessHourId);
    }
}
