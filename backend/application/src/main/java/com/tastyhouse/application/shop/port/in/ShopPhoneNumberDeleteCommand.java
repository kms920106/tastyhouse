package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopPhoneNumberDeleteCommand(
    Long ceoId,
    Long phoneNumberId
) {
    public ShopPhoneNumberDeleteCommand {
        if (ceoId == null || phoneNumberId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopPhoneNumberDeleteCommand of(Long ceoId, Long phoneNumberId) {
        return new ShopPhoneNumberDeleteCommand(ceoId, phoneNumberId);
    }
}
