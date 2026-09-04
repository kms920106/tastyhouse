package com.tastyhouse.ceoapplication.shop.port.in;

import java.time.LocalTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 브레이크타임 등록 command.
 */
public record ShopBreakTimeOwnerCreateCommand(
    Long ceoId,
    Long shopId,
    String dayType,
    LocalTime startTime,
    LocalTime endTime
) {
    public ShopBreakTimeOwnerCreateCommand {
        if (ceoId == null || shopId == null || dayType == null || startTime == null || endTime == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
