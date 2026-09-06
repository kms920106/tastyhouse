package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopRiderVisitGuideDeleteCommand(
    Long shopId,
    Long adminId,
    String reason
) {
    public ShopRiderVisitGuideDeleteCommand {
        if (shopId == null || adminId == null || reason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
