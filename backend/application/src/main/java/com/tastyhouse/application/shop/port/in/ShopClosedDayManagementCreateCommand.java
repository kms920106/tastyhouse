package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopClosedDayManagementCreateCommand(
    Long adminId,
    Long shopId,
    String closedDayType
) {
    public ShopClosedDayManagementCreateCommand {
        if (adminId == null || shopId == null || closedDayType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
