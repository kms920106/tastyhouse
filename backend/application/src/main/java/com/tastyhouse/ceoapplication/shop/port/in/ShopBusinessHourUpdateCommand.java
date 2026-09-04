package com.tastyhouse.ceoapplication.shop.port.in;

import java.time.LocalTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 운영시간 수정 command. 경로 변수 {@code businessHourId}는 컨트롤러가 주입한다.
 */
public record ShopBusinessHourUpdateCommand(
    Long ceoId,
    Long businessHourId,
    String dayType,
    LocalTime openTime,
    LocalTime closeTime,
    Boolean isClosed,
    Boolean is24Hours
) {
    public ShopBusinessHourUpdateCommand {
        if (ceoId == null || businessHourId == null || dayType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
