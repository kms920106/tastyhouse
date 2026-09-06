package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopPhoneNumberPrimaryDesignateCommand(
    Long ceoId,
    Long phoneNumberId
) {
    public ShopPhoneNumberPrimaryDesignateCommand {
        if (ceoId == null || phoneNumberId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopPhoneNumberPrimaryDesignateCommand of(Long ceoId, Long phoneNumberId) {
        return new ShopPhoneNumberPrimaryDesignateCommand(ceoId, phoneNumberId);
    }
}
