package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopDeliveryTipSchedulesUpdateCommand(
    Long ceoId,
    Long shopId,
    List<ShopDeliveryTipScheduleCommand> schedules
) {

    public ShopDeliveryTipSchedulesUpdateCommand {
        if (ceoId == null || shopId == null || schedules == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
