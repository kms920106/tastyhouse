package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopPhoneNumberCreateCommand(
    Long ceoId,
    Long shopId,
    String phoneNumber,
    Boolean virtual
) {

    public ShopPhoneNumberCreateCommand {
        if (ceoId == null || shopId == null || phoneNumber == null || virtual == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
