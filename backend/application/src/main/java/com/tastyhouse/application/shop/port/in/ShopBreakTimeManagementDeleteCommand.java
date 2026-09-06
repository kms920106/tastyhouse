package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopBreakTimeManagementDeleteCommand(
    Long adminId,
    Long breakTimeId
) {
    public ShopBreakTimeManagementDeleteCommand {
        if (adminId == null || breakTimeId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopBreakTimeManagementDeleteCommand of(Long adminId, Long breakTimeId) {
        return new ShopBreakTimeManagementDeleteCommand(adminId, breakTimeId);
    }
}
