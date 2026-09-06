package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopImageChangeRejectCommand(
    Long requestId,
    String reason
) {
    public ShopImageChangeRejectCommand {
        if (requestId == null || reason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
