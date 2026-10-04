package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopPhoneNumberDeleteCommand(
    Long ceoId,
    Long phoneNumberId
) {

    public ShopPhoneNumberDeleteCommand {
        if (ceoId == null || phoneNumberId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopPhoneNumberDeleteCommand of(Long ceoId, Long phoneNumberId) {
        return new ShopPhoneNumberDeleteCommand(ceoId, phoneNumberId);
    }
}
