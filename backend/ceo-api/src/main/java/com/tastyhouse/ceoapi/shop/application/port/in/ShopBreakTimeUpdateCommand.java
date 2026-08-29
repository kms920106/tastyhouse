package com.tastyhouse.ceoapi.shop.application.port.in;

import java.time.LocalTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 브레이크타임 수정 command. 경로 변수 {@code breakTimeId}는 컨트롤러가 주입한다.
 */
public record ShopBreakTimeUpdateCommand(
    Long ceoId,
    Long breakTimeId,
    String dayType,
    LocalTime startTime,
    LocalTime endTime
) {
    public ShopBreakTimeUpdateCommand {
        if (ceoId == null || breakTimeId == null || dayType == null || startTime == null || endTime == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
