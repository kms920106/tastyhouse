package com.tastyhouse.application.shop.port.in;

import java.time.LocalTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 브레이크타임 수정 command. */
public record ShopBreakTimeManagementUpdateCommand(
    Long adminId,
    Long breakTimeId,
    String dayType,
    LocalTime startTime,
    LocalTime endTime
) {
    public ShopBreakTimeManagementUpdateCommand {
        if (adminId == null || breakTimeId == null || dayType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
