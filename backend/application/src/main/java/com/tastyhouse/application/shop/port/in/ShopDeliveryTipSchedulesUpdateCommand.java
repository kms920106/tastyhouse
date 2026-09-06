package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopDeliveryTipSchedulesUpdateCommand(
    Long ceoId,
    Long shopId,
    List<ShopDeliveryTipScheduleCommand> schedules
) {
    public ShopDeliveryTipSchedulesUpdateCommand {
        if (ceoId == null || shopId == null || schedules == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
