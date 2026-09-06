package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopStatusUpdateCommand(
    Long ceoId,
    Long shopId,
    String status
) {
    public ShopStatusUpdateCommand {
        if (ceoId == null || shopId == null || status == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
