package com.tastyhouse.application.shop.port.in;

import java.time.LocalTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 브레이크타임 등록 command. */
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
