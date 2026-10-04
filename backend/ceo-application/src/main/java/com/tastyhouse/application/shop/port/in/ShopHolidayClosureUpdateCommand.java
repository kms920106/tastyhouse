package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopHolidayClosureUpdateCommand(
    Long ceoId,
    Long shopId,
    Boolean closedOnPublicHolidays
) {

    public ShopHolidayClosureUpdateCommand {
        if (ceoId == null || shopId == null || closedOnPublicHolidays == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
