package com.tastyhouse.adminapplication.shop.port.in;

import java.time.LocalTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 운영시간 수정 command. */
public record ShopBusinessHourUpdateCommand(
    Long adminId,
    Long businessHourId,
    String dayType,
    LocalTime openTime,
    LocalTime closeTime,
    Boolean isClosed,
    Boolean is24Hours
) {
    public ShopBusinessHourUpdateCommand {
        if (adminId == null || businessHourId == null || dayType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
