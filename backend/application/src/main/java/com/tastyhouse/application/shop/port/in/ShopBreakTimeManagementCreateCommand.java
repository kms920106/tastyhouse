package com.tastyhouse.application.shop.port.in;

import java.time.LocalTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopBreakTimeManagementCreateCommand(
    Long adminId,
    Long shopId,
    String dayType,
    LocalTime startTime,
    LocalTime endTime
) {
    public ShopBreakTimeManagementCreateCommand {
        if (adminId == null || shopId == null || dayType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
