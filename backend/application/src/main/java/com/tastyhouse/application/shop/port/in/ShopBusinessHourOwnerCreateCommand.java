package com.tastyhouse.application.shop.port.in;

import java.time.LocalTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopBusinessHourOwnerCreateCommand(
    Long ceoId,
    Long shopId,
    String dayType,
    LocalTime openTime,
    LocalTime closeTime,
    Boolean isClosed,
    Boolean is24Hours
) {
    public ShopBusinessHourOwnerCreateCommand {
        if (ceoId == null || shopId == null || dayType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
