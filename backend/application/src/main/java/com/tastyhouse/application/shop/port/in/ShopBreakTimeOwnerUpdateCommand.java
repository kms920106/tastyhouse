package com.tastyhouse.application.shop.port.in;

import java.time.LocalTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopBreakTimeOwnerUpdateCommand(
    Long ceoId,
    Long breakTimeId,
    String dayType,
    LocalTime startTime,
    LocalTime endTime
) {
    public ShopBreakTimeOwnerUpdateCommand {
        if (ceoId == null || breakTimeId == null || dayType == null || startTime == null || endTime == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
