package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopPhoneNumberPrimaryDesignateCommand(
    Long ceoId,
    Long phoneNumberId
) {

    public ShopPhoneNumberPrimaryDesignateCommand {
        if (ceoId == null || phoneNumberId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopPhoneNumberPrimaryDesignateCommand of(Long ceoId, Long phoneNumberId) {
        return new ShopPhoneNumberPrimaryDesignateCommand(ceoId, phoneNumberId);
    }
}
