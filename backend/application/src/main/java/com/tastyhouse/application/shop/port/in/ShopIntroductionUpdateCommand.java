package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopIntroductionUpdateCommand(
    Long ceoId,
    Long shopId,
    String message
) {
    public ShopIntroductionUpdateCommand {
        if (ceoId == null || shopId == null || message == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
