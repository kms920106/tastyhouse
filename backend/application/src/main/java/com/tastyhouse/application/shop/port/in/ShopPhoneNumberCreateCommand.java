package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopPhoneNumberCreateCommand(
    Long ceoId,
    Long shopId,
    String phoneNumber,
    Boolean virtual
) {
    public ShopPhoneNumberCreateCommand {
        if (ceoId == null || shopId == null || phoneNumber == null || virtual == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
