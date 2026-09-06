package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopClosedDayManagementDeleteCommand(
    Long adminId,
    Long closedDayId
) {
    public ShopClosedDayManagementDeleteCommand {
        if (adminId == null || closedDayId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopClosedDayManagementDeleteCommand of(Long adminId, Long closedDayId) {
        return new ShopClosedDayManagementDeleteCommand(adminId, closedDayId);
    }
}
